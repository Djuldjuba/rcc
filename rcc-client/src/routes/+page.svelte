<script>
	import MainPageMenuItem from "$lib/components/MainPageMenuItem.svelte";
</script>


<nav aria-label="Основная навигация">
	<h1 class="text-3xl text-center m-14">Ваши любимые картины и художники всегда рядом</h1>

	<ul class="grid grid-cols-1 md:grid-cols-3 gap-8 p-4 w-4/5 mx-auto">
		<MainPageMenuItem
				link="/painting"
				linkText="Картины"
				src="/piones-dtuchs.jpeg"
				altText="Перейти к галерее картин"
		/>
		<MainPageMenuItem
				link="/artist"
				linkText="Художники"
				src="/renuar.jpeg"
				altText="Перейти к списку художников"
		/>
		<MainPageMenuItem
				link="/museum"
				linkText="Музеи"
				src="/hermitage.jpg"
				altText="Перейти к списку музеев"
		/>
	</ul>
</nav>



