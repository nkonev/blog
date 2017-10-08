<template>
    <div>
        <center>See ViewEditCancel</center>
        <ViewEditCancel
                :loadingMessage="'loading post from server'"
                :canEdit="true" :canDelete="true"
                :onDelete="onDeleteInVEC"
                :onSave="onSaveInVEC"
                :isValid="validateInVEC">

        </ViewEditCancel>
    </div>
</template>

<script>
    import ViewEditCancel from './ViewEditCancel.vue';

    export default {
        components: {
            ViewEditCancel
        },
        methods:{
            onDeleteInVEC(){
                console.log('deleting!');
            },
            validateInVEC(){
                console.log('validating...');
                return true;
            },
            onSaveInVEC() {
                console.log('saving...');
                return new Promise((resolve, reject) => {
                    setTimeout(() => {
                        resolve(true);
                        //reject(false);
                    }, 1000);
                });
            },
        },
    }
</script>