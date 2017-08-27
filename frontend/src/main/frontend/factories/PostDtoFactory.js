const def = () => {
    return {
        id: 0,
        title: '',
        text: '',
        titleImg: '',
        canEdit: false,
        canDelete: false
    }
};

export default def;

export const loading = () => {
    const post = def();
    post.title = "Loading...";
    return post;
};
