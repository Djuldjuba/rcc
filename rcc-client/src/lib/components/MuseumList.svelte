<script lang="ts">
    import { museumsStore } from '$lib/stores/museum.store';
    
    let loadedImages = new Set<string>();
    
    function handleImageLoad(id: string) {
        loadedImages.add(id);
        loadedImages = loadedImages;
    }
</script>

<ul class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4 p-4">
    {#each $museumsStore.data as museum(museum.id)}
        <li class="relative">
            <a href={`/museum/${museum.id}`}>
                {#if !loadedImages.has(museum.id)}
                    <div class="skeleton h-96 w-full rounded-lg"></div>
                {/if}
                <img 
                    class="max-w-full rounded-lg object-cover w-full h-96 transition-opacity duration-300"
                    class:opacity-0={!loadedImages.has(museum.id)}
                    class:absolute={!loadedImages.has(museum.id)}
                    on:load={() => handleImageLoad(museum.id)}
                    src={museum.photo} 
                    alt={museum.title}
                    loading="lazy"
                    decoding="async"
                >
                <div class="text-center mt-2">{museum.title}</div>
                <div class="text-center">{museum.geo.city}, {museum.geo.country.name}</div>
            </a>
        </li>
    {/each}
</ul>
