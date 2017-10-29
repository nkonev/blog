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
    return vue.$route.params.id;
};

export const getTimestampFromUtc = (utcString) => {
    const gmtDateTime = moment.utc(utcString, "YYYY-MM-DD HH:mm:ss.SSS");
    const local = gmtDateTime.local().format('YYYY-MM-DD HH:mm:ss');
    return local;
};