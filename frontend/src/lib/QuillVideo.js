// https://github.com/quilljs/quill/issues/1707#issuecomment-331808561
import VQuill from 'quill'
const Quill = window.Quill || VQuill

var BlockEmbed = Quill.import('blots/block/embed');

class Video extends BlockEmbed {
    static create(value) {
        let node = super.create(value);
        let iframe = document.createElement('iframe');
        iframe.setAttribute('frameborder', '0');
        iframe.setAttribute('allowfullscreen', true);
        iframe.setAttribute('src', value);
        node.appendChild(iframe);
        return node;
    }

    static value(domNode) {
        return domNode.firstChild.getAttribute('src');
    }
}
Video.blotName = 'video';
Video.className = 'ql-video';
Video.tagName = 'div';


export default Video;