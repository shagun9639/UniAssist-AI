package com.uniassist.dto;

import java.util.List;

public class ModelInfoResponse {
    private String modelName;
    private String version;
    private String algorithm;
    private String trainedAt;
    private Integer totalDatasetSize;
    private Double testAccuracy;
    private Double macroF1;
    private List<String> supportedIntents;

    public ModelInfoResponse() {}

    public ModelInfoResponse(String modelName, String version, String algorithm, String trainedAt,
                             Integer totalDatasetSize, Double testAccuracy, Double macroF1, List<String> supportedIntents) {
        this.modelName = modelName;
        this.version = version;
        this.algorithm = algorithm;
        this.trainedAt = trainedAt;
        this.totalDatasetSize = totalDatasetSize;
        this.testAccuracy = testAccuracy;
        this.macroF1 = macroF1;
        this.supportedIntents = supportedIntents;
    }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

    public String getTrainedAt() { return trainedAt; }
    public void setTrainedAt(String trainedAt) { this.trainedAt = trainedAt; }

    public Integer getTotalDatasetSize() { return totalDatasetSize; }
    public void setTotalDatasetSize(Integer totalDatasetSize) { this.totalDatasetSize = totalDatasetSize; }

    public Double getTestAccuracy() { return testAccuracy; }
    public void setTestAccuracy(Double testAccuracy) { this.testAccuracy = testAccuracy; }

    public Double getMacroF1() { return macroF1; }
    public void setMacroF1(Double macroF1) { this.macroF1 = macroF1; }

    public List<String> getSupportedIntents() { return supportedIntents; }
    public void setSupportedIntents(List<String> supportedIntents) { this.supportedIntents = supportedIntents; }
}
