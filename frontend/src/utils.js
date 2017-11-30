import moment from 'moment'

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
    const gmtDateTime = moment.utc(utcString, "YYYY-MM-DD HH:mm:ss.SSS");
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
