package com.nttdata.hackathon.gameotron;

import com.ibm.watson.developer_cloud.http.HttpMediaType;
import com.ibm.watson.developer_cloud.http.ServiceCall;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.KeywordsResult;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.Transcript;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by HUETTM on 23.06.2017.
 */
@Path("trivia")
public class TriviaService {

    private TextToSpeech textToSpeechService;

    private Voice englishVoice;

    private SpeechToText speechToTextService;

    private HashMap<String, QuestionAndAnswer> qandA = new HashMap<String, QuestionAndAnswer>();

    private final String startText = "Welcome to the Gamotron trivia quiz! I will ask you a question and you can just tell me your answer. The first question is:";

    private final String endText = "Thank you for playing!";

    private final String correctText = "The answer is correct!";

    private final String wrongText = "Your answer is not correct! The correct answer would have been: ";

    private byte[] correctTextAudioBuffer;

    private byte[] startTextAudioBuffer;

    private static final Logger logger = LogManager.getLogger(TriviaService.class);

    public TriviaService(){
        qandA.put("1", new QuestionAndAnswer("What kind of dog is Snoopy from the Peanuts?", "Beagle"));
        qandA.put("2", new QuestionAndAnswer("Natural pearls are found in what sea creature?", "Oyster"));
        qandA.put("3", new QuestionAndAnswer("Granny Smith is the name of a popular type of which fruit?", "Apple"));
        qandA.put("4", new QuestionAndAnswer("What is the second largest country by land mass?", "Canada"));
        qandA.put("5",new QuestionAndAnswer("Star Trek: The Next Generation originally aired in what year?", "1987"));
        qandA.put("6", new QuestionAndAnswer("How many letters has the greek alphabet?", "24"));
        qandA.put("7", new QuestionAndAnswer("Shinto is the indigenous faith of what country?", "Japan"));
        qandA.put("8", new QuestionAndAnswer("What is the name of hte instrument used to measure earthquakes?", "Seismometer"));
        qandA.put("9",new QuestionAndAnswer("What digital currency is Satoshi Nakamoto credited with inventing?", "Bitcoin"));
        qandA.put("10", new QuestionAndAnswer("In the movie 'Bambi' what type of animal is Bambi's friend Flower?", "Skunk"));

        textToSpeechService = new TextToSpeech();
        textToSpeechService.setUsernameAndPassword("6c3e76bf-8b4f-49fe-9d5c-e2be53c51d14", "7HgtjCuwOH17");
        textToSpeechService.setEndPoint("https://stream-fra.watsonplatform.net/text-to-speech/api");

        speechToTextService = new SpeechToText();
        speechToTextService.setUsernameAndPassword("cb3d3285-1ffd-40b4-8bc5-c1f044c72250", "312xLXuoOT3O");
        speechToTextService.setEndPoint("https://stream-fra.watsonplatform.net/speech-to-text/api");

        englishVoice = textToSpeechService.getVoice("en-US_AllisonVoice").execute();
    }

    /**
     * Get a new question as WAV stream
     * @param questionId
     * @return
     */
    @GET
    @Path("question/{questionId}")
    @Produces("audio/wav")
    public Response getQuestion(@PathParam("questionId") String questionId){
        Response response = Response.status(Response.Status.NOT_FOUND).build();
        if(qandA.containsKey(questionId)){
            String question = qandA.get(questionId).getQuestion();
            response = getAudioStreamResponse(question);
        } else if("start".equals(questionId)) {
            response = getAudioStreamResponse(startText);
        } else if("end".equals(questionId)) {
            response = getAudioStreamResponse(endText);
        }
        return response;
    }

    /**
     * Returns the response audio stream using the textToSpeechService.
     * The startText and the correctText will be buffered once synthsized.
     *
     *
     * @param textToSynthsize
     * @return
     */
    private Response getAudioStreamResponse(String textToSynthsize) {
        InputStream audioStream;
        if(startText.equals(textToSynthsize)) {
            if(startTextAudioBuffer != null) {
                audioStream = new ByteArrayInputStream(startTextAudioBuffer);
            } else {
                ServiceCall<InputStream> textToSpeechServiceCall = textToSpeechService.synthesize(textToSynthsize, englishVoice);
                InputStream firstTimeAudioStream = textToSpeechServiceCall.execute();
                try {
                    startTextAudioBuffer = IOUtils.toByteArray(firstTimeAudioStream);
                    audioStream = new ByteArrayInputStream(startTextAudioBuffer);
                } catch (IOException e) {
                    logger.error("Problem buffering the audio to the startTextAudioBuffer");
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
            }
        } else if (correctText.equals(textToSynthsize)) {
            if(correctTextAudioBuffer != null) {
                audioStream = new ByteArrayInputStream(correctTextAudioBuffer);
            } else {
                ServiceCall<InputStream> textToSpeechServiceCall = textToSpeechService.synthesize(textToSynthsize, englishVoice);
                InputStream firstTimeAudioStream = textToSpeechServiceCall.execute();
                try {
                    correctTextAudioBuffer = IOUtils.toByteArray(firstTimeAudioStream);
                    audioStream = new ByteArrayInputStream(correctTextAudioBuffer);
                } catch (IOException e) {
                    logger.error("Problem buffering the audio to the startTextAudioBuffer");
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
            }
        } else {
            Response response;ServiceCall<InputStream> textToSpeechServiceCall = textToSpeechService.synthesize(textToSynthsize, englishVoice);
            audioStream = textToSpeechServiceCall.execute();
        }

        return Response.ok().entity(audioStream).build();
    }

    /**
     * Get the answer or reponse to a question as WAV stream and the link to a follow-up question
     *
     * @param questionId - the questionId where you want to get the answer to
     * @param audioInputStream - the answer from the user as WAV file.
     * @return
     */
    @POST
    @Path("answer/{questionId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("audio/wav")
    public Response getAnswer(@PathParam("questionId") String questionId, @FormDataParam("audio") InputStream audioInputStream) {
        ArrayList<Transcript> results = new ArrayList<>();

        final CountDownLatch speechServiceLatch = new CountDownLatch(1);

        String answer = qandA.get(questionId).getAnswer();
        RecognizeOptions regonizeOptionsForSpeech = new RecognizeOptions.Builder()
                .continuous(false)
                .interimResults(false)
                .contentType(HttpMediaType.AUDIO_WAV)
                .keywords(answer)
                .keywordsThreshold(0.7)
                .build();

        speechToTextService.recognizeUsingWebSocket(audioInputStream, regonizeOptionsForSpeech, new BaseRecognizeCallback() {
            @Override
            public void onTranscription(SpeechResults speechResults) {
                Transcript result = speechResults.getResults().stream().filter(transcript -> transcript.isFinal()).findFirst().get();
                results.add(result);
                speechServiceLatch.countDown();
            }
        });

        try {
            speechServiceLatch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            if (results.isEmpty()) {
                logger.warn("Interrupted but no result!");
                return Response.noContent().build();
            }
        }

        Transcript recognitionResult = results.get(0);

        Response response;

        if(recognitionResult.getKeywordsResult().get(answer) != null){
            response = getAudioStreamResponse(correctText);
        } else {
            response = getAudioStreamResponse(wrongText + answer);
        }

        return response;
    }
}
