/*
Created 10/26/2022 by Alex Yarantsev
*/
import com.google.gdata.util.ContentType
import wslite.http.auth.HTTPBasicAuthorization;
import wslite.rest.RESTClient;
import wslite.rest.Response;
import com.sustain.security.model.*;

import com.sustain.rule.model.RuleDef;
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import groovy.json.*;
import java.lang.Exception;
import java.io.*;
import java.time.*;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;

String messageSource = SystemProperty.getValue("interface.source");
String apimEventEndpoint = SystemProperty.getValue("interface.apimOASIS.endpoint");

// Parse the incoming JSON string
def slurper = new JsonSlurper()
def message = slurper.parseText(_message);


messageToOASIS = [
        ruleCode: "FIND_23RCASES_FOR_DIR",
        inputParams: [
                params: [[
                                 name: "body",
                                 value: _message
                         ]
                ]
        ]
]

logger.debug("messageToOASIS " + messageToOASIS)


try {

    // Send the message to APIM endpoint
    RESTClient client = new RESTClient(SystemProperty.getValue("interface.apimOASIS.endpoint").toString());
    client.authorization = new HTTPBasicAuthorization(SystemProperty.getValue("interface.apim.username").toString(), SystemProperty.getValue("interface.apim.password").toString());

    restResponse1 = client.post(path: '/ws/rest/ecourt/executeRule') {
        type ContentType.JSON
        json messageToOASIS
    }
    logger.debug("54 restResponse1.json " + restResponse1.json)
    logger.debug("55 restResponse1.contentAsString :" + restResponse1.contentAsString)
    _responseMessage = restResponse1.contentAsString;
    _responseCode = 200;

} catch(Exception ex) {
    //Catch block
    _responseCode = 500;
    _responseMessage = _responseCode + ": " +  ex.toString();
}


//test message = {"inspNum":"1467641"}




