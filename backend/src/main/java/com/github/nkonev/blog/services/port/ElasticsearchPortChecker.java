package com.github.nkonev.blog.services.port;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

@Service(ElasticsearchPortChecker.NAME)
public class ElasticsearchPortChecker extends AbstractPortChecker {

    public static final String NAME="elasticsearchPortChecker";

    @Value("${port.check.elasticsearch.max.count:512}")
    private int maxCount;

    @Autowired
    private ElasticsearchProperties elasticsearchProperties;

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchPortChecker.class);

    @PostConstruct
    public void checkPorts(){
        String clusterNodes = elasticsearchProperties.getClusterNodes();
        String[] nodes = StringUtils.delimitedListToStringArray(clusterNodes, ",");
        for (String node: nodes) {
            String[] segments = StringUtils.delimitedListToStringArray(node, ":");

            String host = segments[0].trim();
            int port = Integer.valueOf(segments[1].trim());

            LOGGER.info("Checking node {}", node);
            check(maxCount, host, port);
        }
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
