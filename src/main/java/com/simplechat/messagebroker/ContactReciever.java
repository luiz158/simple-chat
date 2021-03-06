package com.simplechat.messagebroker;

import com.simplechat.exception.NotFoundException;
import com.simplechat.model.Contact;
import com.simplechat.model.User;
import com.simplechat.service.UserService;
import com.simplechat.util.api.ResponseJsonGenerator;
import com.simplechat.websocket.MessageHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Mohsen Jahanshahi
 */
@Service
public class ContactReciever {

    private static final Logger LOG = LoggerFactory.getLogger(ContactReciever.class);

    @Autowired
    UserService userService;

    @Autowired
    MessageHandler messageHandler;

    @KafkaListener(topics = "CONTACTS_IMPORT_CONTACTS")
    public void importContacts(@Payload String message, @Headers MessageHeaders headers) {

        // send to socket
        JSONObject jsonObject = new JSONObject(message);

        String authKey = jsonObject.getString("auth_key");
        JSONArray contactsArr = jsonObject.getJSONArray("contacts");
        boolean replace = jsonObject.getBoolean("replace");

        Set<Contact> contacts = new HashSet<>();

        for (int i = 0; i < contactsArr.length(); i++) {

            JSONObject contactObj = contactsArr.getJSONObject(i);
            contacts.add(new Contact(
                    contactObj.getString("mobile"),
                    contactObj.getString("first_name"),
                    contactObj.getString("last_name")
            ));
        }

        // get userId from authkey
        String userId;
        try {
            userId = userService.getUserIdByAuthKey(authKey);
        } catch (NotFoundException e) {
            return;
        }

        // import contact
        userService.importContacts(userId, contacts, replace);

        String data =  ResponseJsonGenerator.createSuccesResponseEntity();

        try {
            messageHandler.sendMessageToUser(userId, data);
        } catch (IOException e) {
            LOG.warn("error sending data to user : " + userId, e);
            return;
        }
    }

    /**
     * return all contact of user
     * @param message
     * @param headers
     */
    @KafkaListener(topics = "CONTACTS_GET_CONTACTS")
    public void getContacts(@Payload String message, @Headers MessageHeaders headers) {

        // send to socket
        JSONObject jsonObject = new JSONObject(message);

        String authKey = jsonObject.getString("auth_key");

        // get user id by auth key
        String userId;
        try {
            userId = userService.getUserIdByAuthKey(authKey);
        } catch (NotFoundException e) {
            return;
        }


        // get all contacts from persistence
        Set<Contact> contacts = userService.getContacts(UUID.fromString(userId));

        // wrap data with json
        JSONArray contactsArr = new JSONArray();
        for(Contact contact : contacts) {

            JSONObject contactObj = new JSONObject();
            contactObj.put("mobile", contact.getMobile());
            contactObj.put("first_name", contact.getFname());
            contactObj.put("last_name", contact.getLname());
            contactObj.put("id", contact.getUserId());

            contactsArr.put(contactObj);
        }

        // send data to user
        try {
            String data =  ResponseJsonGenerator.createSuccesResponseEntity(new JSONObject().put("contacts", contactsArr));
            messageHandler.sendMessageToUser(userId, data);
            
        } catch (IOException e) {
            LOG.warn("error sending data to user : " + userId, e);
        }
    }


}
