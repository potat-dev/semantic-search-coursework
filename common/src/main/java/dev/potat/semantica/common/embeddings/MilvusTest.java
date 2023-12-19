package dev.potat.semantica.common.embeddings;

import java.util.*;

public class MilvusTest {
    public static void main(String[] args) {
        MilvusWrapper milvus = MilvusWrapper.getInstance("192.168.1.40", 19530);
        System.out.println(milvus.listCollections());
//        milvus.insertEmbedding("test", generateVector());
//        milvus.insertEmbedding("test1dfs", generateVector());
//        milvus.insertEmbedding("testdfsd", generateVector());
//        milvus.insertEmbedding("tes423t", generateVector());
//        milvus.insertEmbedding("tes65756432t", generateVector());
//
        milvus.search(generateVector());
//        milvus.query("446407842836266657");
    }

    public static List<Float> generateVector() {
        List<Float> list = new LinkedList<>();
        float rangeMin = -1.0f;
        float rangeMax = 1.0f;
        Random r = new Random();
        for (int i = 0; i < 768; i++) {
            float createdRanNum = rangeMin + (rangeMax - rangeMin) * r.nextFloat();
            list.add(createdRanNum);
        }
        return list;
    }
}
