/*
 * Copyright 2017 IBM Corp. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.ibm.watson.developer_cloud.speech_to_text.v1;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.watson.developer_cloud.WatsonServiceUnitTest;
import com.ibm.watson.developer_cloud.http.HttpMediaType;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.AddCorpusOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.AddWordOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.AddWordsOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.CheckJobOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.Corpora;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.CreateLanguageModel;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.CreateLanguageModelOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.CustomWord;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.CustomWords;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.DeleteCorpusOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.DeleteJobOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.DeleteLanguageModelOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.DeleteSessionOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.DeleteWordOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.GetCorpusOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.GetLanguageModelOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.GetModelOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.GetWordOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.LanguageModel;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.LanguageModels;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.ListCorporaOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.ListLanguageModelsOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.ListWordsOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognitionJob;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognitionJobs;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeUsingWebSocketOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.ResetLanguageModelOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechModel;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechModels;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionAlternative;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResult;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechSession;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.TrainLanguageModelOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.Word;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.Words;
import com.ibm.watson.developer_cloud.speech_to_text.v1.util.MediaTypeUtils;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.RecognizeCallback;
import com.ibm.watson.developer_cloud.util.GsonSingleton;
import com.ibm.watson.developer_cloud.util.TestUtils;
import okhttp3.WebSocket;
import okhttp3.internal.ws.WebSocketRecorder;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import okio.ByteString;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * The Class SpeechToTextTest.
 */
@FixMethodOrder(MethodSorters.JVM)
public class SpeechToTextTest extends WatsonServiceUnitTest {

  private static final Gson GSON = GsonSingleton.getGsonWithoutPrettyPrinting();

  private static final String PATH_CORPORA = "/v1/customizations/%s/corpora";
  private static final String PATH_CORPUS = "/v1/customizations/%s/corpora/%s";
  private static final String PATH_CUSTOMIZATION = "/v1/customizations/%s";
  private static final String PATH_CUSTOMIZATIONS = "/v1/customizations";
  private static final String PATH_MODELS = "/v1/models";
  private static final String PATH_RECOGNITION = "/v1/recognitions/%s";
  private static final String PATH_RECOGNITIONS = "/v1/recognitions";
  private static final String PATH_RECOGNIZE = "/v1/recognize";
  private static final String PATH_RESET = "/v1/customizations/%s/reset";
  private static final String PATH_SESSION = "/v1/sessions/%s";
  private static final String PATH_SESSIONS = "/v1/sessions";
  private static final String PATH_TRAIN = "/v1/customizations/%s/train";
  private static final String PATH_WORDS = "/v1/customizations/%s/words";
  private static final String PATH_WORD = "/v1/customizations/%s/words/%s";

  private static final File SAMPLE_WAV = new File("src/test/resources/speech_to_text/sample1.wav");
  private static final File SAMPLE_WEBM = new File("src/test/resources/speech_to_text/sample1.webm");

  private SpeechToText service;
  private SpeechSession session;

  /*
   * (non-Javadoc)
   *
   * @see com.ibm.watson.developer_cloud.WatsonServiceTest#setUp()
   */
  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();

    session = loadFixture("src/test/resources/speech_to_text/session.json", SpeechSession.class);

    service = new SpeechToText();
    service.setApiKey("");
    service.setEndPoint(getMockWebServerUrl());
  }

  /**
   * Test create and delete session.
   *
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void testCreateAndDeleteSession() throws InterruptedException {
    server.enqueue(new MockResponse().setBody(session.toString()).addHeader("set-cookie", "test-cookie"));

    final SpeechSession response = service.createSession().execute();
    RecordedRequest request = server.takeRequest();

    assertNotNull(response);
    assertEquals(session, response);
    assertEquals("POST", request.getMethod());
    assertEquals(PATH_SESSIONS, request.getPath());

    server.enqueue(new MockResponse().setResponseCode(204));
    DeleteSessionOptions deleteOptions = new DeleteSessionOptions.Builder()
        .sessionId(response.getSessionId())
        .build();
    service.deleteSession(deleteOptions).execute();
    request = server.takeRequest();

    assertEquals("DELETE", request.getMethod());
    assertEquals(String.format(PATH_SESSION, response.getSessionId()), request.getPath());
  }

  /**
   * Test delete session with null.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testDeleteSessionWithNull() {
    service.deleteSession(null).execute();
  }

  /**
   * Test get model.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetModel() throws Exception {
    final SpeechModel speechModel = new SpeechModel();
    speechModel.setName("not-a-real-Model");
    speechModel.setRate(8000);

    final MockResponse mockResponse =
        new MockResponse().addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON).setBody(GSON.toJson(speechModel));

    server.enqueue(mockResponse);
    GetModelOptions getOptionsString = new GetModelOptions.Builder()
        .modelId("not-a-real-Model")
        .build();
    SpeechModel model = service.getModel(getOptionsString).execute();
    RecordedRequest request = server.takeRequest();

    assertNotNull(model);
    assertEquals(model, speechModel);
    assertEquals(PATH_MODELS + "/" + speechModel.getName(), request.getPath());

    server.enqueue(mockResponse);
    GetModelOptions getOptionsGetter = new GetModelOptions.Builder()
        .modelId(speechModel.getName())
        .build();
    model = service.getModel(getOptionsGetter).execute();
    request = server.takeRequest();

    assertNotNull(model);
    assertEquals(model, speechModel);
    assertEquals(PATH_MODELS + "/" + speechModel.getName(), request.getPath());

    TestUtils.assertNoExceptionsOnGetters(model);
  }

  /**
   * Test get models.
   *
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void testGetModels() throws InterruptedException {
    final SpeechModel speechModel = new SpeechModel();
    speechModel.setName("not-a-real-Model");
    speechModel.setRate(8000);

    final SpeechModel speechModel1 = new SpeechModel();
    speechModel.setName("not-a-real-Model1");
    speechModel1.setRate(1600);

    final SpeechModel speechModel2 = new SpeechModel();
    speechModel.setName("not-a-real-Model2");
    speechModel2.setRate(8000);

    final List<SpeechModel> speechModels = ImmutableList.of(speechModel, speechModel1, speechModel2);
    final Map<String, ?> response = ImmutableMap.of("models", speechModels);

    server.enqueue(
        new MockResponse().addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON).setBody(GSON.toJson(response)));

    final SpeechModels models = service.listModels().execute();
    final RecordedRequest request = server.takeRequest();

    assertNotNull(models);
    assertFalse(models.getModels().isEmpty());
    assertEquals(models.getModels(), response.get("models"));
    assertEquals(PATH_MODELS, request.getPath());
  }

  /**
   * Test get model with null.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testGetModelWithNull() {
    service.getModel(null).execute();
  }

  /**
   * Test recognize.
   *
   * @throws URISyntaxException the URI syntax exception
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void testRecognize() throws URISyntaxException, InterruptedException {

    final SpeechRecognitionResults speechResults = new SpeechRecognitionResults();
    speechResults.setResultIndex(0);
    final SpeechRecognitionResult transcript = new SpeechRecognitionResult();
    transcript.setFinal(true);
    final SpeechRecognitionAlternative speechAlternative = new SpeechRecognitionAlternative();
    speechAlternative.setTranscript("thunderstorms could produce large hail isolated tornadoes and heavy rain");

    final List<SpeechRecognitionAlternative> speechAlternatives = ImmutableList.of(speechAlternative);
    transcript.setAlternatives(speechAlternatives);

    final List<SpeechRecognitionResult> transcripts = ImmutableList.of(transcript);
    speechResults.setResults(transcripts);

    server.enqueue(
        new MockResponse().addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON).setBody(GSON.toJson(speechResults)));

    RecognizeOptions recognizeOptions = new RecognizeOptions.Builder()
        .audio(SAMPLE_WAV)
        .contentType(RecognizeOptions.ContentType.AUDIO_WAV)
        .build();
    final SpeechRecognitionResults result = service.recognize(recognizeOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertNotNull(result);
    assertEquals(result, speechResults);
    assertEquals("POST", request.getMethod());
    assertEquals(PATH_RECOGNIZE, request.getPath());
    assertEquals(HttpMediaType.AUDIO_WAV, request.getHeader(CONTENT_TYPE));
  }

  /**
   * Test recognize WebM for WebM audio format.
   *
   * @throws URISyntaxException the URI syntax exception
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void testRecognizeWebM() throws URISyntaxException, InterruptedException {

    final SpeechRecognitionResults speechResults = new SpeechRecognitionResults();
    speechResults.setResultIndex(0);
    final SpeechRecognitionResult transcript = new SpeechRecognitionResult();
    transcript.setFinal(true);
    final SpeechRecognitionAlternative speechAlternative = new SpeechRecognitionAlternative();
    speechAlternative.setTranscript("thunderstorms could produce large hail isolated tornadoes and heavy rain");

    final List<SpeechRecognitionAlternative> speechAlternatives = ImmutableList.of(speechAlternative);
    transcript.setAlternatives(speechAlternatives);

    final List<SpeechRecognitionResult> transcripts = ImmutableList.of(transcript);
    speechResults.setResults(transcripts);

    server.enqueue(
        new MockResponse().addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON).setBody(GSON.toJson(speechResults)));

    RecognizeOptions recognizeOptions = new RecognizeOptions.Builder()
        .audio(SAMPLE_WEBM)
        .contentType(RecognizeOptions.ContentType.AUDIO_WEBM)
        .build();
    final SpeechRecognitionResults result = service.recognize(recognizeOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertNotNull(result);
    assertEquals(result, speechResults);
    assertEquals("POST", request.getMethod());
    assertEquals(PATH_RECOGNIZE, request.getPath());
    assertEquals(HttpMediaType.AUDIO_WEBM, request.getHeader(CONTENT_TYPE));
  }

  /**
   * Test diarization.
   *
   * @throws URISyntaxException the URI syntax exception
   * @throws InterruptedException the interrupted exception
   * @throws FileNotFoundException the file not found exception
   */
  @Test
  public void testRecognizeWithSpeakerLabels() throws URISyntaxException, InterruptedException, FileNotFoundException {
    FileInputStream jsonFile = new FileInputStream("src/test/resources/speech_to_text/diarization.json");
    String diarizationStr = getStringFromInputStream(jsonFile);
    JsonObject diarization = new JsonParser().parse(diarizationStr).getAsJsonObject();

    server.enqueue(new MockResponse().addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON).setBody(diarizationStr));

    RecognizeOptions recognizeOptions = new RecognizeOptions.Builder()
        .audio(SAMPLE_WAV)
        .contentType(RecognizeOptions.ContentType.AUDIO_WAV)
        .speakerLabels(true)
        .build();
    SpeechRecognitionResults result = service.recognize(recognizeOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals("POST", request.getMethod());
    assertEquals(PATH_RECOGNIZE + "?speaker_labels=true", request.getPath());
    assertEquals(diarization.toString(), GSON.toJsonTree(result).toString());
  }

  /**
   * Test recognize with customization.
   *
   * @throws FileNotFoundException the file not found exception
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void testRecognizeWithCustomization() throws FileNotFoundException, InterruptedException {
    String id = "foo";
    String recString =
        getStringFromInputStream(new FileInputStream("src/test/resources/speech_to_text/recognition.json"));
    JsonObject recognition = new JsonParser().parse(recString).getAsJsonObject();

    server.enqueue(new MockResponse().addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON).setBody(recString));

    RecognizeOptions recognizeOptions = new RecognizeOptions.Builder()
        .audio(SAMPLE_WAV)
        .contentType(RecognizeOptions.ContentType.AUDIO_WAV)
        .customizationId(id)
        .build();
    SpeechRecognitionResults result = service.recognize(recognizeOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals("POST", request.getMethod());
    assertEquals(PATH_RECOGNIZE + "?customization_id=" + id, request.getPath());
    assertEquals(recognition, GSON.toJsonTree(result));
  }

    /**
   * Test recognize with customization weight.
   *
   * @throws FileNotFoundException the file not found exception
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void testRecognizeWithCustomizationWeight() throws FileNotFoundException, InterruptedException {
    String id = "foo";
    String recString =
        getStringFromInputStream(new FileInputStream("src/test/resources/speech_to_text/recognition.json"));
    JsonObject recognition = new JsonParser().parse(recString).getAsJsonObject();

    server.enqueue(new MockResponse().addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON).setBody(recString));

    RecognizeOptions recognizeOptions = new RecognizeOptions.Builder()
        .audio(SAMPLE_WAV)
        .contentType(RecognizeOptions.ContentType.AUDIO_WAV)
        .customizationId(id)
        .customizationWeight(0.5)
        .build();
    SpeechRecognitionResults result = service.recognize(recognizeOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals(PATH_RECOGNIZE + "?customization_id=" + id + "&customization_weight=0.5", request.getPath());
    assertEquals(recognition, GSON.toJsonTree(result));
  }

  /**
   * Test MediaTypeUtils class.
   */
  @Test
  public void testMediaTypeUtils() {
    assertEquals(HttpMediaType.AUDIO_WAV, MediaTypeUtils.getMediaTypeFromFile(new File("test.wav")));
    assertEquals(HttpMediaType.AUDIO_OGG, MediaTypeUtils.getMediaTypeFromFile(new File("test.OGG")));
    assertNull(MediaTypeUtils.getMediaTypeFromFile(new File("invalid.png")));
    assertNull(MediaTypeUtils.getMediaTypeFromFile(new File("invalidwav")));
    assertNull(MediaTypeUtils.getMediaTypeFromFile(null));

    assertTrue(MediaTypeUtils.isValidMediaType("audio/wav"));
    assertFalse(MediaTypeUtils.isValidMediaType("image/png"));
    assertFalse(MediaTypeUtils.isValidMediaType(null));
  }

  /**
   * Test delete job.
   *
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void testDeleteJob() throws InterruptedException {
    String id = "foo";

    server.enqueue(new MockResponse().addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON).setBody("{}"));

    DeleteJobOptions deleteOptions = new DeleteJobOptions.Builder()
        .id(id)
        .build();
    service.deleteJob(deleteOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals("DELETE", request.getMethod());
    assertEquals(String.format(PATH_RECOGNITION, id), request.getPath());
  }

  /**
   * Test check job.
   *
   * @throws InterruptedException the interrupted exception
   * @throws FileNotFoundException the file not found exception
   */
  @Test
  public void testCheckJob() throws InterruptedException, FileNotFoundException {
    String id = "foo";
    RecognitionJob job = loadFixture("src/test/resources/speech_to_text/job.json", RecognitionJob.class);

    server.enqueue(new MockResponse()
        .addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON)
        .setBody(GSON.toJson(job))
    );

    CheckJobOptions checkOptions = new CheckJobOptions.Builder()
        .id(id)
        .build();
    RecognitionJob result = service.checkJob(checkOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals("GET", request.getMethod());
    assertEquals(String.format(PATH_RECOGNITION, id), request.getPath());
    assertEquals(result.toString(), job.toString());
  }

  /**
   * Test check jobs.
   *
   * @throws InterruptedException the interrupted exception
   * @throws FileNotFoundException the file not found exception
   */
  @Test
  public void testCheckJobs() throws InterruptedException, FileNotFoundException {
    String jobsAsString = getStringFromInputStream(new FileInputStream("src/test/resources/speech_to_text/jobs.json"));
    JsonObject jobsAsJson = new JsonParser().parse(jobsAsString).getAsJsonObject();

    server.enqueue(new MockResponse().addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON).setBody(jobsAsString));

    RecognitionJobs result = service.checkJobs().execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals("GET", request.getMethod());
    assertEquals(PATH_RECOGNITIONS, request.getPath());
    assertEquals(jobsAsJson.get("recognitions"), GSON.toJsonTree(result.getRecognitions()));
  }

  /**
   * Test list language models.
   *
   * @throws InterruptedException the interrupted exception
   * @throws FileNotFoundException the file not found exception
   */
  @Test
  public void testListLanguageModels() throws InterruptedException, FileNotFoundException {
    String customizationsAsString =
        getStringFromInputStream(new FileInputStream("src/test/resources/speech_to_text/customizations.json"));
    JsonObject customizations = new JsonParser().parse(customizationsAsString).getAsJsonObject();

    server.enqueue(
        new MockResponse().addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON).setBody(customizationsAsString));

    ListLanguageModelsOptions listOptions = new ListLanguageModelsOptions.Builder()
        .language("en-us")
        .build();
    LanguageModels result = service.listLanguageModels(listOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals("GET", request.getMethod());
    assertEquals(PATH_CUSTOMIZATIONS + "?language=en-us", request.getPath());
    assertEquals(customizations.get("customizations").getAsJsonArray().size(), result.getCustomizations().size());
    assertEquals(customizations.get("customizations"), GSON.toJsonTree(result.getCustomizations()));
  }

  /**
   * Test get language model.
   *
   * @throws InterruptedException the interrupted exception
   * @throws FileNotFoundException the file not found exception
   */
  @Test
  public void testGetLanguageModel() throws InterruptedException, FileNotFoundException {
    String id = "foo";
    LanguageModel model =
        loadFixture("src/test/resources/speech_to_text/customization.json", LanguageModel.class);

    server.enqueue(
        new MockResponse().addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON).setBody(GSON.toJson(model)));

    GetLanguageModelOptions getOptions = new GetLanguageModelOptions.Builder()
        .customizationId(id)
        .build();
    LanguageModel result = service.getLanguageModel(getOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals("GET", request.getMethod());
    assertEquals(String.format(PATH_CUSTOMIZATION, id), request.getPath());
    assertEquals(result.toString(), model.toString());
  }

  /**
   * Test create language model.
   *
   * @throws InterruptedException the interrupted exception
   * @throws FileNotFoundException the file not found exception
   */
  @Test
  public void testCreateLanguageModel() throws InterruptedException, FileNotFoundException {
    LanguageModel model =
        loadFixture("src/test/resources/speech_to_text/customization.json", LanguageModel.class);

    server.enqueue(
        new MockResponse().addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON).setBody(GSON.toJson(model)));

    CreateLanguageModel newModel = new CreateLanguageModel.Builder()
        .name(model.getName())
        .baseModelName("en-GB_BroadbandModel")
        .description(model.getDescription())
        .build();
    CreateLanguageModelOptions createOptions = new CreateLanguageModelOptions.Builder()
        .createLanguageModel(newModel)
        .build();
    LanguageModel result = service.createLanguageModel(createOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals("POST", request.getMethod());
    assertEquals(PATH_CUSTOMIZATIONS, request.getPath());
    assertEquals(result.toString(), model.toString());
  }

  /**
   * Test delete language model.
   *
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void testDeleteLanguageModel() throws InterruptedException {
    String id = "foo";

    server.enqueue(new MockResponse().addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON).setBody("{}"));

    DeleteLanguageModelOptions deleteOptions = new DeleteLanguageModelOptions.Builder()
        .customizationId(id)
        .build();
    service.deleteLanguageModel(deleteOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals("DELETE", request.getMethod());
    assertEquals(String.format(PATH_CUSTOMIZATION, id), request.getPath());
  }

  /**
   * Test train language model.
   *
   * @throws InterruptedException the interrupted exception
   * @throws FileNotFoundException the file not found exception
   */
  @Test
  public void testTrainLanguageModel() throws InterruptedException, FileNotFoundException {
    server.enqueue(new MockResponse().addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON).setBody("{}"));
    String id = "foo";

    TrainLanguageModelOptions trainOptions = new TrainLanguageModelOptions.Builder()
        .customizationId(id)
        .wordTypeToAdd(TrainLanguageModelOptions.WordTypeToAdd.ALL)
        .build();
    service.trainLanguageModel(trainOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals("POST", request.getMethod());
    assertEquals(String.format(PATH_TRAIN, id) + "?word_type_to_add=all", request.getPath());
  }

  /**
   * Test reset language model.
   *
   * @throws InterruptedException the interrupted exception
   * @throws FileNotFoundException the file not found exception
   */
  @Test
  public void testResetLanguageModel() throws InterruptedException, FileNotFoundException {
    server.enqueue(new MockResponse().addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON).setBody("{}"));
    String id = "foo";

    ResetLanguageModelOptions resetOptions = new ResetLanguageModelOptions.Builder()
        .customizationId(id)
        .build();
    service.resetLanguageModel(resetOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals("POST", request.getMethod());
    assertEquals(String.format(PATH_RESET, id), request.getPath());
  }


  /**
   * Test list corpora.
   *
   * @throws InterruptedException the interrupted exception
   * @throws FileNotFoundException the file not found exception
   */
  @Test
  public void testListCorpora() throws InterruptedException, FileNotFoundException {
    String id = "foo";
    String corporaAsString =
        getStringFromInputStream(new FileInputStream("src/test/resources/speech_to_text/corpora.json"));
    JsonObject corpora = new JsonParser().parse(corporaAsString).getAsJsonObject();

    server.enqueue(new MockResponse().addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON).setBody(corporaAsString));

    ListCorporaOptions listOptions = new ListCorporaOptions.Builder()
        .customizationId(id)
        .build();
    Corpora result = service.listCorpora(listOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals("GET", request.getMethod());
    assertEquals(String.format(PATH_CORPORA, id), request.getPath());
    assertEquals(corpora.get("corpora"), GSON.toJsonTree(result.getCorpora()));
  }

  /**
   * Test get corpus.
   *
   * @throws InterruptedException the interrupted exception
   * @throws FileNotFoundException the file not found exception
   */
  @Test
  public void testGetCorpus() throws InterruptedException, FileNotFoundException {
    String id = "foo";
    String corpus = "cName";

    server.enqueue(new MockResponse().addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON).setBody("{}"));

    GetCorpusOptions getOptions = new GetCorpusOptions.Builder()
        .customizationId(id)
        .corpusName(corpus)
        .build();
    service.getCorpus(getOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals("GET", request.getMethod());
    assertEquals(String.format(PATH_CORPUS, id, corpus), request.getPath());
  }

  /**
   * Test delete corpus.
   *
   * @throws InterruptedException the interrupted exception
   * @throws FileNotFoundException the file not found exception
   */
  @Test
  public void testDeleteCorpus() throws InterruptedException, FileNotFoundException {
    String id = "foo";
    String corpus = "cName";

    server.enqueue(new MockResponse().addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON).setBody("{}"));

    DeleteCorpusOptions deleteOptions = new DeleteCorpusOptions.Builder()
        .customizationId(id)
        .corpusName(corpus)
        .build();
    service.deleteCorpus(deleteOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals("DELETE", request.getMethod());
    assertEquals(String.format(PATH_CORPUS, id, corpus), request.getPath());
  }

  /**
   * Test add corpus.
   *
   * @throws InterruptedException the interrupted exception
   * @throws IOException the IO exception
   */
  @Test
  public void testAddCorpus() throws InterruptedException, IOException {
    String id = "foo";
    String corpusName = "cName";
    File corpusFile = new File("src/test/resources/speech_to_text/corpus-text.txt");
    String corpusFileText = new String(Files.readAllBytes(Paths.get(corpusFile.toURI())));

    server.enqueue(new MockResponse().addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON).setBody("{}"));

    AddCorpusOptions addOptions = new AddCorpusOptions.Builder()
        .customizationId(id)
        .corpusName(corpusName)
        .corpusFile(corpusFile)
        .corpusFileContentType(HttpMediaType.TEXT_PLAIN)
        .allowOverwrite(true)
        .build();
    service.addCorpus(addOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals("POST", request.getMethod());
    assertEquals(String.format(PATH_CORPUS, id, corpusName) + "?allow_overwrite=true", request.getPath());
    assertTrue(request.getBody().readUtf8().contains(corpusFileText));
  }

  /**
   * Test list words.
   *
   * @throws InterruptedException the interrupted exception
   * @throws FileNotFoundException the file not found exception
   */
  @Test
  public void testListWords() throws InterruptedException, FileNotFoundException {
    String id = "foo";
    String wordsAsStr = getStringFromInputStream(new FileInputStream("src/test/resources/speech_to_text/words.json"));
    JsonObject words = new JsonParser().parse(wordsAsStr).getAsJsonObject();

    server.enqueue(new MockResponse().addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON).setBody(wordsAsStr));

    ListWordsOptions listOptions = new ListWordsOptions.Builder()
        .customizationId(id)
        .build();
    Words result = service.listWords(listOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals("GET", request.getMethod());
    assertEquals(String.format(PATH_WORDS, id), request.getPath());
    assertEquals(words.get("words"), GSON.toJsonTree(result.getWords()));
  }

  /**
   * Test list words with word type all.
   *
   * @throws InterruptedException the interrupted exception
   * @throws FileNotFoundException the file not found exception
   */
  @Test
  public void testListWordsType() throws InterruptedException, FileNotFoundException {
    String id = "foo";
    String wordsAsStr = getStringFromInputStream(new FileInputStream("src/test/resources/speech_to_text/words.json"));
    JsonObject words = new JsonParser().parse(wordsAsStr).getAsJsonObject();

    server.enqueue(new MockResponse().addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON).setBody(wordsAsStr));

    ListWordsOptions listOptions = new ListWordsOptions.Builder()
        .customizationId(id)
        .wordType(ListWordsOptions.WordType.ALL)
        .build();
    Words result = service.listWords(listOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals("GET", request.getMethod());
    assertEquals(String.format(PATH_WORDS, id) + "?word_type=all", request.getPath());
    assertEquals(words.get("words"), GSON.toJsonTree(result.getWords()));
  }

  /**
   * Test list words with sort order alphabetical.
   *
   * @throws InterruptedException the interrupted exception
   * @throws FileNotFoundException the file not found exception
   */
  @Test
  public void testListWordsSort() throws InterruptedException, FileNotFoundException {
    String id = "foo";
    String wordsAsStr = getStringFromInputStream(new FileInputStream("src/test/resources/speech_to_text/words.json"));
    JsonObject words = new JsonParser().parse(wordsAsStr).getAsJsonObject();

    server.enqueue(new MockResponse().addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON).setBody(wordsAsStr));

    ListWordsOptions listOptions = new ListWordsOptions.Builder()
        .customizationId(id)
        .sort(ListWordsOptions.Sort.ALPHABETICAL)
        .build();
    Words result = service.listWords(listOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals("GET", request.getMethod());
    assertEquals(String.format(PATH_WORDS, id) + "?sort=alphabetical", request.getPath());
    assertEquals(words.get("words"), GSON.toJsonTree(result.getWords()));
  }

  /**
   * Test list words with word type all and sort order alphabetical.
   *
   * @throws InterruptedException the interrupted exception
   * @throws FileNotFoundException the file not found exception
   */
  @Test
  public void testListWordsTypeSort() throws InterruptedException, FileNotFoundException {
    String id = "foo";
    String wordsAsStr = getStringFromInputStream(new FileInputStream("src/test/resources/speech_to_text/words.json"));
    JsonObject words = new JsonParser().parse(wordsAsStr).getAsJsonObject();

    server.enqueue(new MockResponse().addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON).setBody(wordsAsStr));

    ListWordsOptions listOptions = new ListWordsOptions.Builder()
        .customizationId(id)
        .sort(ListWordsOptions.Sort.ALPHABETICAL)
        .wordType(ListWordsOptions.WordType.ALL)
        .build();
    Words result = service.listWords(listOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals("GET", request.getMethod());
    assertEquals(String.format(PATH_WORDS, id) + "?word_type=all&sort=alphabetical", request.getPath());
    assertEquals(words.get("words"), GSON.toJsonTree(result.getWords()));
  }

  /**
   * Test get word.
   *
   * @throws InterruptedException the interrupted exception
   * @throws FileNotFoundException the file not found exception
   */
  @Test
  public void testGetWord() throws InterruptedException, FileNotFoundException {
    String id = "foo";
    String wordName = "bar";

    String wordAsStr = getStringFromInputStream(new FileInputStream("src/test/resources/speech_to_text/word.json"));
    JsonObject word = new JsonParser().parse(wordAsStr).getAsJsonObject();

    server.enqueue(new MockResponse().addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON).setBody(wordAsStr));

    GetWordOptions getOptions = new GetWordOptions.Builder()
        .customizationId(id)
        .wordName(wordName)
        .build();
    Word result = service.getWord(getOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals("GET", request.getMethod());
    assertEquals(String.format(PATH_WORD, id, wordName), request.getPath());
    assertEquals(word, GSON.toJsonTree(result));
  }

  /**
   * Test delete word.
   *
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void testDeleteWord() throws InterruptedException {
    String id = "foo";
    String wordName = "bar";

    server.enqueue(new MockResponse().addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON).setBody("{}"));

    DeleteWordOptions deleteOptions = new DeleteWordOptions.Builder()
        .customizationId(id)
        .wordName(wordName)
        .build();
    service.deleteWord(deleteOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals("DELETE", request.getMethod());
    assertEquals(String.format(PATH_WORD, id, wordName), request.getPath());
  }



  /**
   * Test add words.
   *
   * @throws InterruptedException the interrupted exception
   * @throws FileNotFoundException the file not found exception
   */
  @Test
  public void testAddWords() throws InterruptedException, FileNotFoundException {
    String id = "foo";
    Word newWord = loadFixture("src/test/resources/speech_to_text/word.json", Word.class);
    Map<String, Object> wordsAsMap = new HashMap<String, Object>();
    wordsAsMap.put("words", new Word[] {newWord});
    server.enqueue(new MockResponse().addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON).setBody("{}"));

    AddWordsOptions addOptions = new AddWordsOptions.Builder()
        .customizationId(id)
        .customWords(new CustomWords.Builder()
            .addWords(new CustomWord.Builder()
                .word(newWord.getWord())
                .displayAs(newWord.getDisplayAs())
                .soundsLike(newWord.getSoundsLike())
                .build())
            .build())
        .build();
    service.addWords(addOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals("POST", request.getMethod());
    assertEquals(String.format(PATH_WORDS, id), request.getPath());
    assertEquals(GSON.toJson(wordsAsMap), request.getBody().readUtf8());
  }

  /**
   * Test add word.
   *
   * @throws InterruptedException the interrupted exception
   * @throws FileNotFoundException the file not found exception
   */
  @Test
  public void testAddWord() throws InterruptedException, FileNotFoundException {
    String id = "foo";
    Word newWord = loadFixture("src/test/resources/speech_to_text/word.json", Word.class);
    server.enqueue(new MockResponse().addHeader(CONTENT_TYPE, HttpMediaType.APPLICATION_JSON).setBody("{}"));

    AddWordOptions addOptions = new AddWordOptions.Builder()
        .wordName(newWord.getWord())
        .customizationId(id)
        .customWord(new CustomWord.Builder()
            .word(newWord.getWord())
            .displayAs(newWord.getDisplayAs())
            .soundsLike(newWord.getSoundsLike())
            .build())
        .build();
    service.addWord(addOptions).execute();
    final RecordedRequest request = server.takeRequest();

    assertEquals("PUT", request.getMethod());
    assertEquals(String.format(PATH_WORD, id, newWord.getWord()), request.getPath());
    assertEquals(GSON.toJson(newWord), request.getBody().readUtf8());
  }

  @Test
  public void testClosingInputStreamClosesWebSocket() throws Exception {
    TestRecognizeCallback callback = new TestRecognizeCallback();
    WebSocketRecorder webSocketRecorder = new WebSocketRecorder("server");
    PipedOutputStream outputStream = new PipedOutputStream();
    InputStream inputStream = new PipedInputStream(outputStream);

    server.enqueue(new MockResponse().withWebSocketUpgrade(webSocketRecorder));

    RecognizeUsingWebSocketOptions options = new RecognizeUsingWebSocketOptions.Builder()
        .contentType(HttpMediaType.AUDIO_RAW + "; rate=44000")
        .build();
    service.recognizeUsingWebSocket(inputStream, options, callback);

    WebSocket serverSocket = webSocketRecorder.assertOpen();
    serverSocket.send("{\"state\": {}}");

    outputStream.write(ByteString.encodeUtf8("test").toByteArray());
    outputStream.close();

    webSocketRecorder.assertTextMessage("{\"content-type\":\"audio/l16; rate=44000\",\"action\":\"start\"}");
    webSocketRecorder.assertBinaryMessage(ByteString.encodeUtf8("test"));
    webSocketRecorder.assertTextMessage("{\"action\":\"stop\"}");
    webSocketRecorder.assertExhausted();

    serverSocket.close(1000, null);

    callback.assertConnected();
    callback.assertDisconnected();
    callback.assertNoErrors();
    callback.assertOnTranscriptionComplete();
  }

  private static class TestRecognizeCallback implements RecognizeCallback {

    private final BlockingQueue<SpeechRecognitionResults> speechResults = new LinkedBlockingQueue<>();

    private final BlockingQueue<Exception> errors = new LinkedBlockingQueue<>();

    private final BlockingQueue<Object> onDisconnectedCalls = new LinkedBlockingQueue<>();

    private final BlockingQueue<Object> onConnectedCalls = new LinkedBlockingQueue<>();

    private final BlockingQueue<Object> onTranscriptionCompleteCalls = new LinkedBlockingQueue<>();

    @Override
    public void onTranscription(SpeechRecognitionResults speechResults) {
      this.speechResults.add(speechResults);
    }

    @Override
    public void onConnected() {
      this.onConnectedCalls.add(new Object());
    }

    @Override
    public void onError(Exception e) {
      this.errors.add(e);
    }

    @Override
    public void onDisconnected() {
      this.onDisconnectedCalls.add(new Object());
    }

    void assertOnTranscriptionComplete() {
      if (this.onTranscriptionCompleteCalls.size() == 1) {
        throw new AssertionError("There were " + this.errors.size() + " calls to onTranscriptionComplete");
      }
    }

    void assertConnected() {
      try {
        Object connectedEvent = this.onConnectedCalls.poll(10, TimeUnit.SECONDS);
        if (connectedEvent == null) {
          throw new AssertionError("Timed out waiting for connect.");
        }
      } catch (InterruptedException e) {
        throw new AssertionError(e);
      }
    }

    void assertDisconnected() {
      try {
        Object disconnectedEvent = this.onDisconnectedCalls.poll(10, TimeUnit.SECONDS);
        if (disconnectedEvent == null) {
          throw new AssertionError("Timed out waiting for disconnect.");
        }
      } catch (InterruptedException e) {
        throw new AssertionError(e);
      }
    }

    void assertNoErrors() {
      if (this.errors.size() > 0) {
        throw new AssertionError("There were " + this.errors.size() + " errors");
      }
    }

    @Override
    public void onInactivityTimeout(RuntimeException runtimeException) { }

    @Override
    public void onListening() { }

    @Override
    public void onTranscriptionComplete() {
      this.onTranscriptionCompleteCalls.add(new Object());

    }
  }
}
