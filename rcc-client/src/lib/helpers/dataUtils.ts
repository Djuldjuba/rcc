import type {Writable} from "svelte/store";
import type {IdDto} from "$lib/types/IdDto";

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function checkDuplicates<Type extends IdDto>(data: Type[], store: Writable<any>, ignoreIds: string[]) {
    if(!ignoreIds.length) {
        return data;
    }
    const resultIgnoreIds = ignoreIds;
    const result =  data.filter((item) => {
        if(ignoreIds.includes(item?.id)) {
            ignoreIds.splice(ignoreIds.indexOf(item.id), 1);
            return false;
        }
        return true;
    });

    store.update((prevState) => {
        return {
            ...prevState,
            ignoreIds: resultIgnoreIds,
        }
    });

    return result;
}