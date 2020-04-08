const def = () => {
    return {
        id: 0,
        title: '',
        text: '',
        titleImg: '',
        canEdit: false,
        canDelete: false,
        left: null,
        right: null,
        createDateTime: null,
        editDateTime: null,
        metaDescription: null
    }
};

export default def;

export const loading = () => {
    const post = def();
    post.title = "Loading...";
    return post;
};
