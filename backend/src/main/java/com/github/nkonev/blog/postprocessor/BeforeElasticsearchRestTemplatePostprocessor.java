package com.github.nkonev.blog.postprocessor;

import com.github.nkonev.blog.services.port.ElasticsearchPortChecker;
import org.springframework.boot.autoconfigure.AbstractDependsOnBeanFactoryPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

@Configuration
public class BeforeElasticsearchRestTemplatePostprocessor extends AbstractDependsOnBeanFactoryPostProcessor {

	public BeforeElasticsearchRestTemplatePostprocessor() {
        super(ElasticsearchRestTemplate.class, ElasticsearchPortChecker.NAME);
    }
}
