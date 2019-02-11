/**
 * Maps a string to a value of type T
 */
export interface IDMap<T> {
    [id: string]: T
}

/**
 * Iterate through all values in an IDMap
 */
function idMapIterate<T>(map: IDMap<T>, iterator: (value: T, key: string) => void) {
    for (const id in map) {
        if(map.hasOwnProperty(id)) {
            iterator(map[id], id);
        }
    }
}

/**
 * Get all values in an IDMap
 */
function idMapValues<T>(map: IDMap<T>): T[] {
    const result: T[] = [];

    idMapIterate(map, value => result.push(value));

    return result;
}

// now we can use these functions as if they were static properties of IDMap
export const IDMap = {
    iterate: idMapIterate,
    values: idMapValues,
};
