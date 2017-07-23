package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.PageUtils;
import com.github.nikit.cpp.dto.UserDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(Constants.Uls.API_PUBLIC+Constants.Uls.POSTS)
public class PostsController {

    private static final List<PostDTO> POST_DTO_LIST;
    static {
        POST_DTO_LIST = new ArrayList<>();
        for (int i=0; i<100001; ++i){
            try {
                POST_DTO_LIST.add(new PostDTO(
                        (long)i,
                        "Заголовок поста Заголовок поста Заголовок поста Заголовок поста Заголовок поста Заголовок поста Заголовок поста "+i,
                        "Lorem Ipsum - это текст-\"рыба\", часто используемый в печати и вэб-дизайне. Lorem Ipsum является стандартной \"рыбой\" для текстов на латинице с начала XVI века. В то время некий безымянный печатник создал большую коллекцию размеров и форм шрифтов, используя Lorem Ipsum для распечатки образцов. Lorem Ipsum не только успешно пережил без заметных изменений пять веков, но и перешагнул в электронный дизайн. Его популяризации в новое время послужили публикация листов Letraset с образцами Lorem Ipsum в 60-х годах и, в более недавнее время, программы электронной вёрстки типа Aldus PageMaker, в шаблонах которых используется Lorem Ipsum.",
                        new URL("https://vuejs.org/images/components.png")
                ));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }



    public static class PostDTO {
        private long id;
        private String title;
        private String text;
        private URL titleImg;

        public PostDTO() { }

        public PostDTO(long id, String title, String text, URL titleImg) {
            this.id = id;
            this.title = title;
            this.text = text;
            this.titleImg = titleImg;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public URL getTitleImg() {
            return titleImg;
        }

        public void setTitleImg(URL titleImg) {
            this.titleImg = titleImg;
        }
    }

    @GetMapping
    public List<PostDTO> getPosts(
            @RequestParam(value = "page", required=false, defaultValue = "0") int page,
            @RequestParam(value = "size", required=false, defaultValue = "0") int size,
            @RequestParam(value = "searchString", required=false, defaultValue = "") String searchString
    ) {
        page = PageUtils.fixPage(page);
        size = PageUtils.fixSize(size);

        return POST_DTO_LIST.stream().filter(postDTO -> postDTO.getText().contains(searchString) || postDTO.getTitle().contains(searchString)).skip(page*size).limit(size).collect(Collectors.toList());
    }

}
