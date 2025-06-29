
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.bson.Document
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

data class University(
    val country: String,
    val name: String,
    val alpha_two_code: String,
    val state_province: String?,
    val web_pages: List<String>,
    val domains: List<String>
)

object Database {
    private lateinit var collection: MongoCollection<Document>

    fun connect() {
        val mongoClient = MongoClients.create("mongodb://localhost:27017")
        val database: MongoDatabase = mongoClient.getDatabase("university_catalog")
        collection = database.getCollection("universities")
    }

    fun saveUniversities(universities: List<University>) {
        collection.deleteMany(Document())
        universities.forEach { university ->
            val doc = Document()
                .append("country", university.country)
                .append("name", university.name)
                .append("alpha_two_code", university.alpha_two_code)
                .append("state_province", university.state_province)
                .append("web_pages", university.web_pages)
                .append("domains", university.domains)
            collection.insertOne(doc)
        }
        println("Сохранено ${universities.size} университетов в БД.")
    }

    fun searchUniversitiesByName(name: String) {
        val query = Document("name", Document("\$regex", name).append("\$options", "i"))

        val foundUniversities = collection.find(query)

        if (foundUniversities.iterator().hasNext()) {
            println("Найденные университеты:")
            foundUniversities.forEach { universityDocument ->
                val name = universityDocument.getString("name")
                val webPages = universityDocument.getList("web_pages", String::class.java)

                println("Название: $name")
                if (webPages.isNotEmpty()) {
                    println("URL: ${webPages[0]}")
                } else {
                    println("URL: Нет доступных URL")
                }
            }
        } else {
            println("Университеты не найдены.")
        }
    }
}

fun fetchUniversities(country: String): List<University> {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("http://universities.hipolabs.com/search?country=$country")
        .build()

    return try {
        client.newCall(request).execute().use { response: Response ->
            if (!response.isSuccessful) {
                println("Ошибка сети: ${response.message}")
                return emptyList()
            }
            val jsonResponse = response.body!!.string()
            // Не выводим ответ API
            // println("Response: $jsonResponse")
            Gson().fromJson(jsonResponse, object : TypeToken<List<University>>() {}.type)
        }
    } catch (e: IOException) {
        println("Ошибка при получении данных: ${e.message}")
        emptyList()
    }
}

fun main() {
    Database.connect()

    val countryMap = mapOf(
        "Russia" to "Russian Federation",
        "RU" to "Russian Federation"
        // Можно добавить больше стран
    )

    print("Введите страну (например, 'Russian Federation' или 'Russia'): ")
    val inputCountry = readLine() ?: ""
    val country = countryMap[inputCountry] ?: inputCountry

    // Получаем университеты
    val universities = fetchUniversities(country)

    // Сохраняем университеты в БД
    Database.saveUniversities(universities)

    // Поиск университетов
    while (true) {
        print("Введите название университета для поиска (можно часть названия, например 'Amur', или 'exit' для выхода): ")
        val searchQuery = readLine() ?: ""
        if (searchQuery.lowercase() == "exit") {
            break
        }
        Database.searchUniversitiesByName(searchQuery)
    }
}