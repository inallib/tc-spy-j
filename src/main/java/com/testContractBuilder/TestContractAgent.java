package com.testContractBuilder;

import java.util.HashMap;
import java.util.Map;

public class TestContractAgent extends AbstractTestContractBuilder {
    private static TestContractAgent testContractAgent;
    private String packageToScan;

    private TestContractAgent() {}

    public static TestContractAgent activate() {
        if (testContractAgent == null)
            testContractAgent = new TestContractAgent();
        System.out.print("Agent activated");
        return testContractAgent;
    }

    public Map<String, String> build() {
        if (this.packageToScan != null)
            return build(this.packageToScan);
        Map<String, String> contract = new HashMap<String, String>();
        contract.put("0", "No package defined to scan");
        return contract;
    }

    public void setPackageToScan(String packageToScan) {
        this.packageToScan = packageToScan;
    }
}
