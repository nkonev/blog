package com.github.nkonev.blog.repo.elasticsearch;

import com.github.nkonev.blog.entity.elasticsearch.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexPostRepository extends ElasticsearchRepository<Post, Long> {
    @Query("{\n" +
            "  \"query\": {\n" +
            "    \"bool\":{\n" +
            "      \"should\": [\n" +
            "         {\"match_phrase_prefix\": { \"text\": \"?0\" }},\n" +
            "         {\"match_phrase_prefix\": { \"title\": \"?0\" }}\n" +
            "      ]\n" +
            "    }\n" +
            "  },\n" +
            "  \"_source\": [\"text\", \"title\"],\n" +
            "  \"highlight\" : {\n" +
            "        \"fields\" : {\n" +
            "            \"text\" : { \"fragment_size\" : 150, \"pre_tags\" : [\"<em>\"], \"post_tags\" : [\"</em>\"], \"number_of_fragments\" : 5 },\n" +
            "            \"title\" : { \"fragment_size\" : 150, \"pre_tags\" : [\"<em>\"], \"post_tags\" : [\"</em>\"], \"number_of_fragments\" : 1 }\n" +
            "        }\n" +
            "  }\n" +
            "}")
    Page<Post> findWithPrefix(String search, Pageable pageable);
}
