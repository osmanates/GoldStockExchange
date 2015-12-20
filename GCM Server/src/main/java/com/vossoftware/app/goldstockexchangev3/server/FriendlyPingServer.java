/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vossoftware.app.goldstockexchangev3.server;

import com.google.api.client.util.Data;
//import com.google.api.services.datastore.DatastoreV1;
import com.google.api.services.datastore.DatastoreV1;
import com.google.api.services.datastore.client.Datastore;
import com.google.api.services.datastore.client.DatastoreException;
import com.google.api.services.datastore.client.DatastoreFactory;
import com.google.api.services.datastore.client.DatastoreHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import org.jivesoftware.smack.util.StringUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FriendlyPingServer provides the logic to allow clients to register and be notified of other
 * clients registering. It also responds to pings from clients.
 */
public class FriendlyPingServer {

  // FriendlyPing Client.
  private class Client {
    String name;
    @SerializedName("registration_token")
    String registrationToken;
    @SerializedName("profile_picture_url")
    String profilePictureUrl;
    @SerializedName("device_unique_id")
    String deviceUniqueId;

    public boolean isValid() {
      return StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(registrationToken) && StringUtils.isNotEmpty(deviceUniqueId)/* &&
          StringUtils.isNotEmpty(profilePictureUrl)*/;
    }
  }

  // FriendlyGcmServer defines onMessage to handle incoming friendly ping messages.
  private class FriendlyGcmServer extends GcmServer {

    public FriendlyGcmServer(String apiKey, String senderId, String serviceName) {
      super(apiKey, senderId, serviceName);
    }

    @Override
    public void onMessage(String from, JsonObject jData) {
      if (jData.has("action")) {
        String action = jData.get("action").getAsString();
        if (action.equals(Constants.ACTION_REGISTER)) {
          try {
            registerNewClient(jData);
          } catch (DatastoreException e) {
            e.printStackTrace();
          }
        } else if (action.equals(Constants.ACTION_SEND_MESSAGE)) {
          String toToken = jData.get("to").getAsString();
          String senderToken = jData.get("sender").getAsString();
          if (StringUtils.isNotEmpty(toToken) && StringUtils.isNotEmpty(senderToken)) {
            pingClient(toToken, senderToken);
          } else {
            logger.info("Unable to ping unless to and sender tokens are available.");
          }
        }
      } else {
        logger.info("No action found. Message received missing action.");
      }
    }
  }

  private static final Logger logger = Logger.getLogger("FriendlyPingServer");

  private static final String SENDER_ID = "38341071581";
  private static final String API_KEY = "AIzaSyBIbGHL5b0cdqxOIAha5azsklW_23_x3Qg";

  // Actions
  private static final String PING_CLIENT = "ping_client";
  // Keys
  private static final String ACTION_KEY = "action";
  private static final String CLIENT_KEY = "client";
  private static final String CLIENTS_KEY = "clients";
  private static final String DATA_KEY = "data";
  private static final String SENDER_KEY = "sender";

  private static final String NEW_CLIENT_TOPIC = "/topics/newclient";
  private static final String PING_TITLE = "Friendly Ping!";
  private static final String PING_ICON = "mipmap/ic_launcher";
  private static final String CLICK_ACTION = "ping_received";

  public static final String SERVICE_NAME = "Friendly Ping Server";

  // Store of clients registered with FriendlyPingServer.
  private Map<String, Client> clientMap;
  // Listener responsible for handling incoming registrations and pings.
  private FriendlyGcmServer friendlyGcmServer;

  // Gson helper to assist with going to and from JSON and Client.
  private Gson gson;
  private static Datastore datastore;

  public FriendlyPingServer(String apiKey, String senderId) {
    clientMap = new ConcurrentHashMap<String, Client>();

    Client serverClient = createServerClient();
    clientMap.put(serverClient.registrationToken, serverClient);

    gson = new GsonBuilder().create();

    friendlyGcmServer = new FriendlyGcmServer(apiKey, senderId, SERVICE_NAME);
  }

  /**
   * Create a Client object to be used in responses to pings to the server.
   *
   * @return Server Client.
   */
  private Client createServerClient() {
    Client client = new Client();
    client.name = "Larry";
    client.registrationToken = SENDER_ID + "@gcm.googleapis.com";
    client.deviceUniqueId = "abcnfddsd123lkfg";
    client.profilePictureUrl =
            "https://lh3.googleusercontent.com/-Y86IN-vEObo/AAAAAAAAAAI/AAAAAAADO1I/QzjOGHq5kNQ/photo.jpg?sz=50";
    return client;
  }

  /**
   * Create Client from given JSON data, add client to client list, broadcast newly registered
   * client to all previously registered clients and send client list to new client.
   *
   * @param jData JSON data containing properties of new Client.
   */
  private void registerNewClient(JsonObject jData) throws DatastoreException {
    Client newClient = gson.fromJson(jData, Client.class);
    logger.log(Level.INFO, "Name= " + newClient.name);
    logger.log(Level.INFO, "Registration Token=  " + newClient.registrationToken);
    logger.log(Level.INFO, "Picture URL= " + newClient.profilePictureUrl);
    logger.log(Level.INFO, "Device Unique Id= " + newClient.deviceUniqueId);

    if (newClient.isValid()) {

      DatastoreV1.Entity.Builder employee = DatastoreV1.Entity.newBuilder()
              .setKey(DatastoreHelper.makeKey("Person"))
              .addProperty(DatastoreHelper.makeProperty("name", DatastoreHelper.makeValue(newClient.name)))
              .addProperty(DatastoreHelper.makeProperty("regToken", DatastoreHelper.makeValue(newClient.registrationToken)))
              .addProperty(DatastoreHelper.makeProperty("deviceID", DatastoreHelper.makeValue(newClient.deviceUniqueId)))
              .addProperty(DatastoreHelper.makeProperty("profilePic", DatastoreHelper.makeValue(newClient.profilePictureUrl)));
      DatastoreV1.CommitRequest commitRequest = DatastoreV1.CommitRequest.newBuilder()
              .setMode(DatastoreV1.CommitRequest.Mode.NON_TRANSACTIONAL)
              .setMutation(DatastoreV1.Mutation.newBuilder().addInsertAutoId(employee))
              .build();
      DatastoreV1.CommitResponse response = datastore.commit(commitRequest);


      broadcastNewClient(newClient);
      addClient(newClient);
      sendClientList(newClient);
      pingClient(newClient.registrationToken, SENDER_ID + "@" + GcmServer.GCM_HOST);
    } else {
      logger.log(Level.WARNING, "Could not unpack received data into a Client.");
    }
  }

  /**
   * Add given client to Map of Clients.
   *
   * @param client Client to be added.
   */
  private void addClient(Client client) {
    boolean check = false;
    Client clientToReplace = new Client();
    String regId = "";
    for (Map.Entry<String, Client> entry : clientMap.entrySet()) {
      if (client.deviceUniqueId.equals(entry.getValue().deviceUniqueId)) {
        logger.info("ayni device id");
        //clientToReplace.registrationToken = entry.getValue().registrationToken;
        clientToReplace.deviceUniqueId = client.deviceUniqueId;
        clientToReplace.name = client.name;
        clientToReplace.profilePictureUrl = client.profilePictureUrl;
        regId = entry.getKey();
        check = true;
      }
    }

    if (!check) {
      clientMap.put(client.registrationToken, client);
    } else {
      clientToReplace.registrationToken = client.registrationToken;
      clientMap.remove(regId);
      clientMap.put(clientToReplace.registrationToken, clientToReplace);
      logger.info("cihaz listesi");
      for (Map.Entry<String, Client> entry : clientMap.entrySet()) {
        logger.info("cihaz adi= " + entry.getValue().name);
        logger.info("cihaz reg id= " + entry.getValue().registrationToken);
        logger.info("cihaz dev id= " + entry.getValue().deviceUniqueId);
      }
      logger.info("cihaz listesi sonu");

    }
  }

  /**
   * Broadcast the newly registered client to clients that have already been registered. The
   * broadcast is sent via the PubSub topic "/topics/newuser" all registered clients should be
   * subscribed to this topic.
   *
   * @param client Newly registered client.
   */
  private void broadcastNewClient(Client client) {
    JsonObject jBroadcast = new JsonObject();

    JsonObject jData = new JsonObject();
    jData.addProperty(ACTION_KEY, Constants.ACTION_BROADCAST_NEW_CLIENT);

    JsonObject jClient = gson.toJsonTree(client).getAsJsonObject();
    jData.add(CLIENT_KEY, jClient);

    jBroadcast.add(DATA_KEY, jData);
    friendlyGcmServer.send(NEW_CLIENT_TOPIC, jBroadcast);
  }

  /**
   * Send client list to newly registered client. When a new client is registered, that client must
   * be informed about the other registered clients.
   *
   * @param client Newly registered client.
   */
  private void sendClientList(Client client) {
    ArrayList<Client> clientList = new ArrayList();
    for (Entry<String, Client> clientEntry : clientMap.entrySet()) {
      Client currentClient = clientEntry.getValue();
      if (currentClient.registrationToken != client.registrationToken) {
        clientList.add(currentClient);
      }
    }
    JsonElement clientElements = gson.toJsonTree(clientList,
            new TypeToken<Collection<Client>>() {
            }.getType());
    if (clientElements.isJsonArray()) {
      JsonObject jSendClientList = new JsonObject();

      JsonObject jData = new JsonObject();
      jData.addProperty(ACTION_KEY, Constants.ACTION_SEND_CLIENT_LIST);
      jData.add(CLIENTS_KEY, clientElements);

      jSendClientList.add(DATA_KEY, jData);
      friendlyGcmServer.send(client.registrationToken, jSendClientList);
    }
  }

  /**
   * Send message to Client with matching toToken. The validity of to and sender tokens
   * should be check before this method is called.
   *
   * @param toToken     Token of recipient of ping.
   * @param senderToken Token of sender of ping.
   */
  private void pingClient(String toToken, String senderToken) {
    Client senderClient;
    // If the server is the recipient of the ping, send ping to sender, otherwise send ping to
    // toToken.
    if (toToken.equals(SENDER_ID + "@" + GcmServer.GCM_HOST)) {
      senderClient = clientMap.get(toToken);
      toToken = senderToken;
    } else {
      senderClient = clientMap.get(senderToken);
    }
    JsonObject jPing = new JsonObject();

    JsonObject jData = new JsonObject();
    jData.addProperty(ACTION_KEY, Constants.ACTION_SEND_MESSAGE);
    jData.addProperty(SENDER_KEY, senderClient.registrationToken);

    // Create notification that is handled appropriately on the receiving platform.
    JsonObject jNotification = new JsonObject();
    jNotification.addProperty("body", senderClient.name + " is pinging you.");
    jNotification.addProperty("title", PING_TITLE);
    jNotification.addProperty("icon", PING_ICON);
    jNotification.addProperty("sound", "default");
    jNotification.addProperty("click_action", CLICK_ACTION);

    jPing.add(DATA_KEY, jData);
    jPing.add("notification", jNotification);

    friendlyGcmServer.send(toToken, jPing);
  }

  public static Datastore getDatastore(String datasetId) {
    //Datastore v2 =
    try {
      // Setup the connection to Google Cloud Datastore and infer credentials
      // from the environment.
      datastore = DatastoreFactory.get().create(DatastoreHelper.getOptionsFromEnv()
              .dataset(datasetId).build());
      logger.info("successfully connected to the dtastore");
      return datastore;
    } catch (GeneralSecurityException exception) {
      System.err.println("Security error connecting to the datastore: " + exception.getMessage());
      System.exit(1);
      return null;
    } catch (IOException exception) {
      System.err.println("I/O error connecting to the datastore: " + exception.getMessage());
      System.exit(1);
      return null;
    }
  }

  public static void main(String[] args) {
    if (args.length < 1) {
      System.err.println("Usage: FriendlyPingServer <DATASET_ID>");
      System.exit(1);
    }
    // Set the dataset from the command line parameters.
    String datasetId = args[0];
    Datastore datastore = null;
    datastore = getDatastore(datasetId);
    //Datastore v2 =
/*    try {
      // Setup the connection to Google Cloud Datastore and infer credentials
      // from the environment.
      datastore = DatastoreFactory.get().create(DatastoreHelper.getOptionsFromEnv()
              logger.info("successfully connected to the dtastore");
    } catch (GeneralSecurityException exception) {
      System.err.println("Security error connecting to the datastore: " + exception.getMessage());
      System.exit(1);
    } catch (IOException exception) {
      System.err.println("I/O error connecting to the datastore: " + exception.getMessage());
      System.exit(1);
    }*/
    // Initialize FriendlyPingServer with appropriate API Key and SenderID.
    new FriendlyPingServer(API_KEY, SENDER_ID);
    // Keep main thread alive.
    try {
      CountDownLatch latch = new CountDownLatch(1);
      latch.await();
    } catch (InterruptedException e) {
      logger.log(Level.SEVERE, "An error occurred while latch was waiting.", e);
    }
  }
}
