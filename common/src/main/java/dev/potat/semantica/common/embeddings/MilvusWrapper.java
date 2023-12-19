package dev.potat.semantica.common.embeddings;

import io.milvus.client.MilvusClient;
import io.milvus.client.MilvusServiceClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.grpc.DataType;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.SearchResults;
import io.milvus.param.*;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.DropCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.highlevel.collection.ListCollectionsParam;
import io.milvus.param.highlevel.collection.response.ListCollectionsResponse;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.SearchResultsWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MilvusWrapper {
    private static final String COLLECTION_NAME = "links";
    private static final int VECTOR_DIM = 768;
    private static MilvusWrapper instance;
    private final MilvusClient client;

    public MilvusWrapper(String host, int port) {
        ConnectParam connectParam = ConnectParam.newBuilder().withHost(host).withPort(port).build();
        this.client = new MilvusServiceClient(connectParam);
//        drop();
        checkAndInit();
    }

    private void drop() {
        DropCollectionParam dropParam = DropCollectionParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .build();

        R<RpcStatus> response = client.dropCollection(dropParam);
        if (response.getStatus() != R.Status.Success.getCode()) {
            System.out.println(response.getMessage());
        }
    }

    private void init() {
        List<FieldType> fieldsSchema = new ArrayList<>();
        FieldType field_1 = FieldType.newBuilder()
                .withPrimaryKey(true)
                .withAutoID(true)
                .withDataType(DataType.Int64)
                .withName("id")
                .withDescription("unique id")
                .build();

        fieldsSchema.add(field_1);

        FieldType field_2 = FieldType.newBuilder()
                .withDataType(DataType.FloatVector)
                .withName("vector")
                .withDescription("embeddings")
                .withDimension(VECTOR_DIM)
                .build();
        fieldsSchema.add(field_2);

        FieldType field_3 = FieldType.newBuilder()
                .withDataType(DataType.VarChar)
                .withName("url")
                .withMaxLength(512)
                .withDescription("page url")
                .build();
        fieldsSchema.add(field_3);

        // create collection
        CreateCollectionParam createCollectionParam = CreateCollectionParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withDescription("semantica links collection")
                .withFieldTypes(fieldsSchema)
                .build();

        CreateIndexParam createIndexParam = CreateIndexParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withFieldName("vector")
                .withIndexName("vector_idx")
                .withMetricType(MetricType.L2)
                .withIndexType(IndexType.AUTOINDEX)
                .build();

        LoadCollectionParam loadCollectionParam = LoadCollectionParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withSyncLoad(Boolean.TRUE).build();

        R<RpcStatus> response = client.createCollection(createCollectionParam);
        if (response.getStatus() != R.Status.Success.getCode()) {
            System.out.println(response.getMessage());
        }

        response = client.createIndex(createIndexParam);
        if (response.getStatus() != R.Status.Success.getCode()) {
            System.out.println(response.getMessage());
        }

        response = client.loadCollection(loadCollectionParam);
        if (response.getStatus() != R.Status.Success.getCode()) {
            System.out.println(response.getMessage());
        }
    }

    public static MilvusWrapper getInstance(String host, int port) {
        if (instance == null) {
            instance = new MilvusWrapper(host, port);
        }
        return instance;
    }

    public List<String> listCollections() {
        ListCollectionsParam param = ListCollectionsParam.newBuilder().build();

        R<ListCollectionsResponse> response = client.listCollections(param);
        if (response.getStatus() != R.Status.Success.getCode()) {
            System.out.println(response.getMessage());
        }

        return response.getData().collectionNames;
    }

    public void checkAndInit() {
        if (!listCollections().contains(COLLECTION_NAME)) {
            init();
        }
    }

    public void insertEmbedding(String url, List<Float> vector) {
        List<InsertParam.Field> fieldsInsert = new ArrayList<>();
        fieldsInsert.add(new InsertParam.Field("url", Collections.singletonList(url)));
        fieldsInsert.add(new InsertParam.Field("vector", Collections.singletonList(vector)));

        InsertParam param = InsertParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withFields(fieldsInsert)
                .build();

        R<MutationResult> response = client.insert(param);
        if (response.getStatus() != R.Status.Success.getCode()) {
            System.out.println(response.getMessage());
        }
    }

    public void insertEmbeddings(String url, List<List<Float>> vectors) {
        if (vectors.size() == 0) return;

        List<InsertParam.Field> fieldsInsert = new ArrayList<>();
        List<String> urls = Collections.nCopies(vectors.size(), url);
        fieldsInsert.add(new InsertParam.Field("url", urls));
        fieldsInsert.add(new InsertParam.Field("vector", vectors));

        InsertParam param = InsertParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withFields(fieldsInsert)
                .build();

        R<MutationResult> response = client.insert(param);
        if (response.getStatus() != R.Status.Success.getCode()) {
            System.out.println(response.getMessage());
        }
    }

    public void search(List<Float> vector) {
        SearchParam param = SearchParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withMetricType(MetricType.L2)
                .withTopK(10)
                .withVectors(Collections.singletonList(vector))
                .addOutField("url")
                .withVectorFieldName("vector")
                .withConsistencyLevel(ConsistencyLevelEnum.EVENTUALLY)
                .build();
        R<SearchResults> response = client.search(param);
        if (response.getStatus() != R.Status.Success.getCode()) {
            System.out.println(response.getMessage());
        }

        SearchResultsWrapper wrapper = new SearchResultsWrapper(response.getData().getResults());
        System.out.println("Search results:");
        List<SearchResultsWrapper.IDScore> scores = wrapper.getIDScore(0);
        for (SearchResultsWrapper.IDScore score : scores) {
            System.out.println(score);
        }
    }
}
