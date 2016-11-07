import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;


public class SplainerBot extends TelegramLongPollingBot {


    private static final String PROPERTIES_FILENAME = "youtube.properties";

    private static final long NUMBER_OF_VIDEOS_RETURNED = 5;

    private static YouTube youtube;

    public void onUpdateReceived(Update update) {

        Message message = update.getMessage();

        Properties properties = new Properties();
        try {
            InputStream in = YouTube.Search.class.getResourceAsStream("/" + PROPERTIES_FILENAME);
            properties.load(in);
        }
        catch (IOException e) {
            System.err.println("There was an error reading " + PROPERTIES_FILENAME + ": " + e.getCause()
                    + " : " + e.getMessage());
            System.exit(1);
        }

        if (message != null && message.hasText()) {

            if (message.getText().equals("/start") || message.getText().equals("start")) {
                sendMsg(message, "Hi, I'm a Splainer bot and I can help you in finding the video explanation. \n Use /search or /help query for starting work.");

            } else if (message.getText().equals("/help") || message.getText().equals("help")) {
                sendMsgWithoutReply(message, "Call /search SEARCH_ITEM for search it in youtube " );
            }
                else if (message.getText().substring(0,7).equals("/search")&& !message.getText().replace("/search ", "").equals("")) {

                sendMsg(message, "Sending....");

                try {
                    youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, new HttpRequestInitializer() {
                        public void initialize(HttpRequest request) throws IOException {
                        }
                    }).setApplicationName("youtube-cmdline-search-sample").build();

                    String queryTerm = message.getText().replace("/search ", "");

                    YouTube.Search.List search = youtube.search().list("id,snippet");

                    String apiKey = properties.getProperty("youtube.apikey");
                    search.setKey(apiKey);
                    search.setQ(queryTerm);

                    search.setType("video");
                    search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
                    search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);

                    SearchListResponse searchResponse = search.execute();
                    List<SearchResult> searchResultList = searchResponse.getItems();

                    if (searchResultList != null) {
                        prettyPrint(searchResultList.iterator(), queryTerm, message);
                    }

                } catch (GoogleJsonResponseException e) {
                    System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                            + e.getDetails().getMessage());
                } catch (IOException e) {
                    System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                sendMsgWithoutReply(message, "If you want rate some video - call /rate v1 (v1,v2, etc) command, or call /rate ALL if you want rate all videos");
            }  else if (message.getText().substring(0,5).equals("/rate")&& !message.getText().replace("/rate ", "").equals("")) {
                sendMsgWithoutReply(message,"Thanks for you vote");
            }
            else {
                sendMsg(message, "Sorry, but I don't understand you. Try use /help");
            }
        }
    }

    private void prettyPrint(Iterator<SearchResult> iteratorSearchResults, String query, Message message) {
        if (!iteratorSearchResults.hasNext()) {
            System.out.println(" There aren't any results for your query.");
        }
        while (iteratorSearchResults.hasNext()) {
            SearchResult singleVideo = iteratorSearchResults.next();
            ResourceId rId = singleVideo.getId();

            if (rId.getKind().equals("youtube#video")) {
                String link  = "Link video: https://www.youtube.com/watch?v="+rId.getVideoId();
                sendMsgWithoutReply(message,link);
            }
        }
    }

    private void sendMsg(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(text);
        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMsgWithoutReply(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(false);
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(text);

        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String getBotUsername() {
        return "@Splainer_bot";
    }

    public String getBotToken() {
        return "287408158:AAGZNF7_huXVy2yNE2J1MbRW9sXzZtHONUc";
    }
}
