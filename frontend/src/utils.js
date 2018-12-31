import moment from 'moment'
import Vue from 'vue';
import {POSTS_PAGE_SIZE, POSTS_MAX_PAGES} from './constants'

/**
 *
 * @return true if updated, false if not found
 */
export const updateById = (array, newElement) => {
    return array.find((element, index)=>{
        return element.id === newElement.id ? (array.splice(index, 1, newElement), true) : false;
    });
};

export const getPostId = (vue) => {
    const id = vue.$route.params.id;
    // console.log("getting post id", id);
    return id;
};

export const getTimestampFromUtc = (utcString) => {
    const gmtDateTime = moment.utc(utcString, "YYYY-MM-DDTHH:mm:ss.SSSZ");
    const local = gmtDateTime.local().format('YYYY-MM-DD HH:mm:ss');
    return local;
};

/**
 * Pre cutting (0, 700) in PostController#getPosts
 * @param obj
 */
export const cutPost = (obj) => {
    let temp = obj.text.substring(0, 700);
    const last = temp.slice(-1);
    if (!(last == '.')){
        temp += '...';
    }
    obj.text = temp;
};

export const initStompClient = (connectCallback) => {
    const Stomp = require("@stomp/stompjs/bundles/stomp.umd.js").Stomp; // https://github.com/jmesnil/stomp-websocket/issues/119 https://stomp-js.github.io/stomp-websocket/codo/extra/docs-src/Usage.md.html
    const url = ((window.location.protocol === "https:") ? "wss://" : "ws://") + window.location.host + "/stomp";
    const stompClient = Stomp.client(url);
    stompClient.reconnect_delay = 5000; // reconnect after 5 sec

    let connection = stompClient.connect({}, connectCallback);

    return {stompClient, connection};
};

export const closeStompClient = obj => {
    try {
        obj.connection.disconnect();
    } catch (ignored){}
    obj.stompClient.disconnect();
};

/**
 *
 * @param that should contains these properties: {posts: [], noMoreMessage: "", searchString: ""}
 * @param getWithPaginationUrl
 * @param responseArrayExtractor
 * @param onSuccess
 * @param $state
 */
export const infinitePostsHandlerWithSearch = (that, getWithPaginationUrl, responseArrayExtractor, onSuccess, $state) => {
    Vue.http.get(getWithPaginationUrl, {
        params: {
            page: Math.floor(that.posts.length / POSTS_PAGE_SIZE),
            searchString: that.searchString,
        },
    }).then((res) => {
        onSuccess(res);
        const respObj = responseArrayExtractor(res);
        if (respObj.data.length) {
            const new_array = respObj.data.map((e) => {
                cutPost(e);
                return e;
            });

            that.posts = that.posts.concat(new_array); // add data from server's response
            $state.loaded();

            if (Math.floor(that.posts.length / POSTS_PAGE_SIZE) === POSTS_MAX_PAGES) {
                that.noMoreMessage = `You reached max pages limit (${POSTS_MAX_PAGES}). We want to stop to overwhelming your RAM.`;
                console.log("Overwhelming prevention");
                $state.complete();
            }
            // Prevent infinity loading bug when there server responds is less than POSTS_PAGE_SIZE elements
            if (respObj.data.length < POSTS_PAGE_SIZE) {
                console.log("Loaded less than page size");
                $state.complete();
            }
        } else {
            $state.complete();
        }
    }, err => {
        $state.complete();
    });
};


export const isLargeScreen = () => {
    return window.innerWidth > 969;
};

export const computedCropper = {
    innerWidth(){
        return window.innerWidth - 65
    },
    cropperWidth(){
        return isLargeScreen() ? 800 : computedCropper.innerWidth()
    },
    cropperHeight(){
        return isLargeScreen() ? 600 : Math.round(computedCropper.cropperWidth() * (600.0/800));
    },
    cropperRemoveButtonSize(){
        return !isLargeScreen() ? 30 : 45;
    }
};

export const fixScroll = () => {
    if (window.location.hash) {
        Vue.nextTick(()=>{
            const hash = window.location.hash.substring(1, window.location.hash.length);
            //console.log("hash", hash);
            const element_to_scroll_to = document.getElementById(hash);
            element_to_scroll_to.scrollIntoView();
        })
    }
}