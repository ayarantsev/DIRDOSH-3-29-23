
/*
Created 10/26/2022 by Alex Yarantsev
*/

import groovy.json.JsonSlurper;
import groovy.json.JsonOutput;

import com.sustain.rule.model.RuleDef;
import java.lang.Exception;
import groovy.json.*;


def case23Rs = DomainObject.find(Case.class,
        "caseSubType", "==", '23R',
        "cf_oasisSyncStatus", "!=", "DONE",
        maxResult(100))


//Assembling JSON
for(c in case23Rs){
    //Set Pending stastus for 23R case that about to synced with OASIS
    c.cf_oasisSyncStatus = "PEN"

    def messageJsonMap = [
            inspNum: c.referenceNumber
    ]

    //Send each message
    try {
        String messageJsonStr = JsonOutput.toJson(messageJsonMap);
        //logger.debug(" 42 messageJsonStr ---------> " + messageJsonStr)

        //Save data to the Log table:
        outlog = new Ce_interfaceLog()
        outlog.outgoingMessage = messageJsonStr;
        outlog.type = "DIR-23R-CASE-TRIGGER-1"
        outlog.source = "DIR"
        outlog.status = "PENDING"
        outlog.saveOrUpdate()

        params = ["message": messageJsonStr];
        apiResponse = runRule("EVENT_CALL", params)
        //Get Reaponse from EVENT_CALL
        def responseMessage = apiResponse.getValue('responseMessage');
        def responseCode =  apiResponse.getValue('responseCode');

        // Read ResponseMessage
        JsonSlurper slurper = new JsonSlurper();
        def responseFromOASIS = slurper.parseText(responseMessage)

        def json = slurper.parseText(responseFromOASIS.params.value[0])

        //Save response OASIS message
        if(responseCode == 200){
            logger.info("Got matched Case -------> responseCode " + responseCode);
            inlog = new Ce_interfaceLog()
            inlog.incomingMessage = responseFromOASIS.params.value[0];
            inlog.type = json.type
            inlog.source = json.source
            inlog.status = "PENDING"
            inlog.saveOrUpdate()
            outlog.status = "SENT"
            outlog.saveOrUpdate()
            //mark outgoing message as SENT if there is no matched case in OASIS
        } else if (responseCode == 200 && json.matchedCseMap.size() == 0){
            outlog.status = "SENT"
            outlog.saveOrUpdate()
        }

        //If OASIS do not answer, save outlog.status to Error.
        if(responseCode == 500){
            logger.info("Error.  responseCodee----->" + responseCode);
            outlog.status = "ERROR"
            outlog.saveOrUpdate()
        }


    } catch(Exception ex) {
        //Catch block
        logger.info(ex.toString())
    }
}//finish looping over 23R cases



// Use this code below to test this BR manually
// jsonFromOASIS [params:[[name:result, value:{"id":"5e4be89a-09b2-4ab5-89d8-6ff4470c6bee","records":[{"filingDate":"2017-05-31 00:00:00.0","caseNumber":"1195278","caseSubType":"ECOURT","dispositionDate":"2020-03-10 14:51:25.383","dispositionType":"null","dispositionManner":"null"}]}]]]




