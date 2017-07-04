<template>
    <div>
        <b>#{{item.id}}</b>
        <span>{{ item.value }}</span>
        <abbr>{{item.description}}</abbr>
    </div>
</template>

<script>
    export default {
        props: { item: { required: true } }
    }
</script>