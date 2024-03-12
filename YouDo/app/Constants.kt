import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.calendar.CalendarScopes

object Constants {

    const val CREDENTIALS_FILE_PATH = "credentials.json"

    const val TOKENS_DIRECTORY_PATH = "/tokens"

    val HTTP_TRANSPORT: NetHttpTransport = NethttpTransport()

    const val APPLICATION_NAME = "YouDo"

    val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()
    val SCOPES: List<String> = listOf(CalendarScopes.CALENDAR, "https://www.googleapis.com/auth/calendar.events")
}