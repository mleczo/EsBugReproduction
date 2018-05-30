import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

public class Reproduction {
    private final static String url = "http://127.0.0.1:666/";
    private final static RestTemplate restTemplate = new RestTemplate();
    private final static String node = "5qDWLsx"; //REPLACE IT WITH YOUR NODE NAME

    private static String lastIdxName = "test";


    public static void main(String[] args) {
        instructions();
        createIndex();
        populateEs();
        waitMovingCompleted();
        reproduceBug();
    }

    private static void reproduceBug() {
        for (int i = 0; i < 100; i++) {
            split(200);
            shrink(20);
        }
    }

    static String shrink = "{\n" +
            "  \"settings\": {\n" +
            "    \"index.routing.allocation.require._name\": null,\n" +
            "    \"index.blocks.write\": null,\n" +
            "    \"index.number_of_shards\": shard_count\n" +
            "  }\n" +
            "}";

    private static void shrink(int shard_count) {
        prepareForShrinkOrSplit();
        waitMovingCompleted();
        String newIndex = lastIdxName + "n";
        exchange(lastIdxName + "/_shrink/" + newIndex, shrink.replace("shard_count", String.valueOf(shard_count)), HttpMethod.POST);
        waitMovingCompleted();
        deleteOldIdx(lastIdxName);
        lastIdxName = newIndex;
    }

    private static void deleteOldIdx(String lastIdxName) {
        exchange(lastIdxName, null, HttpMethod.DELETE);
    }

    private static void waitMovingCompleted() {
        exchange("_cluster/health?wait_for_no_relocating_shards=true&timeout=12345666m", null, HttpMethod.GET);
    }

    static String split = "{\n" +
            "  \"settings\": {\n" +
            "    \"index.routing.allocation.require._name\": null,\n" +
            "    \"index.blocks.write\": null,\n" +
            "    \"index.number_of_shards\": shard_count\n" +
            "  }\n" +
            "}";

    private static void split(int shard_count) {
        prepareForShrinkOrSplit();
        waitMovingCompleted();
        String newIndex = lastIdxName + "n";
        exchange(lastIdxName + "/_split/" + newIndex, split.replace("shard_count", String.valueOf(shard_count)), HttpMethod.POST);
        waitMovingCompleted();
        deleteOldIdx(lastIdxName);
        lastIdxName = newIndex;
    }

    public static final String PREPARE_FOR_SHRINKING_SPLITTING = "{\n" +
            "  \"settings\": {\n" +
            "    \"index.routing.allocation.require._name\": \"shrink_node_name\", \n" +
            "    \"index.blocks.write\": true \n" +
            "  }\n" +
            "}";

    private static void prepareForShrinkOrSplit() {
        exchange(lastIdxName + "/_settings", PREPARE_FOR_SHRINKING_SPLITTING.replace("shrink_node_name", node), HttpMethod.PUT);
    }

    private static void createIndex() {
        String prepareIndex = "{\n" +
                "    \"settings\" : {\n" +
                "        \"index\" : {\n" +
                "            \"number_of_shards\" : 20, \n" +
                "            \"number_of_routing_shards\" : 80000 \n" +
                "        }\n" +
                "    }\n" +
                "}";
        exchange("test", prepareIndex, HttpMethod.PUT);
    }

    private static void populateEs() {
        String entry = "{  \"index\": {    \"_index\": \"test\",    \"_type\": \"_doc\"  }}";
        StringBuilder bulkContent = new StringBuilder();
        for (int i = 0; i < 60000; i++) {
            bulkContent.append(entry + "\n");
            bulkContent.append(POJO.get(i).getJson() + "\n");
        }

        exchange("_bulk", bulkContent.toString(), HttpMethod.POST);
    }

    private static void instructions() {
        System.out.println("download es 6.2.4, replace elasticsearch.yml with one included with resources here");
        System.out.println("create 5 copies of it, and run them");
        System.out.println("wait all started, and set node string inside this class");
    }


    protected static String exchange(String request, String payload, HttpMethod httpMethod) {
        return exchangeForObject(request, payload, httpMethod, String.class);
    }

    protected static <T> T exchangeForObject(String request, String payload, HttpMethod
            httpMethod, Class<T> clazz) {
        String endpoint = url + request;
        try {
            HttpHeaders jsonHeaders = new HttpHeaders();
            jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<T> exchange = restTemplate.exchange(endpoint, httpMethod, new HttpEntity<>(payload, jsonHeaders), clazz);
            return exchange.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.out.println(e.getResponseBodyAsString());
            throw e;
        }
    }
}
