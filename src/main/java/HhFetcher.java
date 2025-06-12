import com.google.gson.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

public class HhFetcher {

    private static int lastPage = -1;

    public static String fetchAndFormatVacancies() {
        StringBuilder resultText = new StringBuilder();
        try {
            // гарантированно отличающаяся страница
            int page;
            do {
                page = ThreadLocalRandom.current().nextInt(0, 20);
            } while (page == lastPage);
            lastPage = page;

            String apiUrl = "https://api.hh.ru/vacancies?text=java" +
                    "&per_page=5&page=" + page +
                    "&only_with_salary=true&search_field=name";

            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder responseStr = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                responseStr.append(line);
            }
            in.close();

            JsonObject response = JsonParser.parseString(responseStr.toString()).getAsJsonObject();
            JsonArray items = response.getAsJsonArray("items");

            if (items.size() == 0) {
                return "❗ Не найдено подходящих вакансий.";
            }

            resultText.append("📄 Страница ").append(page + 1)
                    .append(" • ").append(LocalDateTime.now()).append("\n\n");

            for (JsonElement elem : items) {
                JsonObject vacancy = elem.getAsJsonObject();
                String title = vacancy.get("name").getAsString();
                String url = vacancy.get("alternate_url").getAsString();
                String company = vacancy.has("employer") && !vacancy.get("employer").isJsonNull()
                        ? vacancy.getAsJsonObject("employer").get("name").getAsString() : "Не указано";
                String city = vacancy.has("area") && !vacancy.get("area").isJsonNull()
                        ? vacancy.getAsJsonObject("area").get("name").getAsString() : "Не указано";

                resultText.append("📌 *").append(title).append("*\n")
                        .append("🏢 ").append(company).append("\n")
                        .append("📍 ").append(city).append("\n")
                        .append("🔗 ").append(url).append("\n\n");
            }

        } catch (Exception e) {
            resultText.append("❗ Ошибка при получении вакансий.");
            e.printStackTrace();
        }

        return resultText.toString();
    }
}
