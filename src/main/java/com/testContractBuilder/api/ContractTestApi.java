package com.testContractBuilder.api;

import com.testContractBuilder.TestContractAgent;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ContractTestApi {
    @RequestMapping("/tc")
    public Map<String, String> getContract() {
        return TestContractAgent.activate().build();
    }
}
