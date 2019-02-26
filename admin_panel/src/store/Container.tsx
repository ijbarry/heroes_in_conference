/**
 * Basically an abstraction so we don't need for each thing x in the store x, xLoading, xLoaded, xErrorMsg, etc...
 *
 * Pretty much entirely boilerplate code with no real logic, so little need to test
 */

export enum ContainerState {
    EMPTY = "empty", // we have not tried to load data, default null state
    LOADING = "loading", // we are waiting for the data to load
    ERRORED = "errored", // an error occurred when loading the data
    SYNCED = "synced", // our data is in sync with the server
    MODIFIED = "modified", // our data is ahead of the server
    DELETED = "deleted", // our data was deleted client-side
}

export interface EmptyContainer {
    state: ContainerState.EMPTY,
}

function empty(): EmptyContainer {
    return {
        state: ContainerState.EMPTY
    };
}

export interface LoadingContainer {
    state: ContainerState.LOADING,
    timeStartedLoading: number // ms time since we sent the load request
}

function loading(timeStartedLoading: number): LoadingContainer {
    return {
        state: ContainerState.LOADING,
        timeStartedLoading,
    }
}

export interface ErrorState {
    timeErrored: number, // ms time that the first error occurred at
    tries: number, // number of attempts to get the data
    errorMsg: string,
    errorData?: any, // debug data about error
}

export interface ErroredContainer {
    state: ContainerState.ERRORED,
    error: ErrorState,
}

function errored(error: ErrorState): ErroredContainer {
    return {
        state: ContainerState.ERRORED,
        error,
    };
}

export interface SyncedContainer<T> {
    state: ContainerState.SYNCED,
    data: T,
    modified: number, // ms time that this data was last modified
}

function synced<T>(data: T, modifiedTime: number): SyncedContainer<T> {
    return {
        state: ContainerState.SYNCED,
        data,
        modified: modifiedTime,
    };
}

export interface ModifiedContainer<T> {
    state: ContainerState.MODIFIED,
    data: T,
    modified: number, // ms time that this data was changed
    error?: ErrorState, // information about errors that occurred when we are trying to save this data back to the server
}

function modified<T>(data: T, modifiedTime: number, error?: ErrorState): ModifiedContainer<T> {
    return {
        state: ContainerState.MODIFIED,
        data,
        modified: modifiedTime,
        error,
    };
}

export interface DeletedContainer {
    state: ContainerState.DELETED,
    modified: number, // ms time data was changed,
    error?: ErrorState,
}

function deleted(modifiedTime: number, error?: ErrorState): DeletedContainer {
    return {
        state: ContainerState.DELETED,
        modified: modifiedTime,
        error,
    }
}

export type Container<T> =
    EmptyContainer
    | LoadingContainer
    | ErroredContainer
    | SyncedContainer<T>
    | ModifiedContainer<T>
    | DeletedContainer
    ;

/**
 * A container that has been previously loaded successfully
 */
export type LoadedContainer<T> =
    SyncedContainer<T>
    | ModifiedContainer<T>
    | DeletedContainer
    ;

/**
 * These containers are ready to give us data!
 */
export type ReadyContainer<T> =
    SyncedContainer<T>
    | ModifiedContainer<T>
    ;


function isEmpty<T>(c: Container<T>): c is EmptyContainer {
    return c.state === ContainerState.EMPTY;
}

function isLoading<T>(c: Container<T>): c is LoadingContainer {
    return c.state === ContainerState.LOADING;
}

// did we error while loading, not did we error while trying to save data
function isErrored<T>(c: Container<T>): c is ErroredContainer {
    return c.state === ContainerState.ERRORED;
}

function isSynced<T>(c: Container<T>): c is SyncedContainer<T> {
    return c.state === ContainerState.SYNCED;
}

function isModified<T>(c: Container<T>): c is ModifiedContainer<T> {
    return c.state === ContainerState.MODIFIED;
}

function isDeleted<T>(c: Container<T>): c is DeletedContainer {
    return c.state === ContainerState.DELETED;
}

function isReady<T>(c: Container<T>): c is ReadyContainer<T> {
    return c.state === ContainerState.SYNCED || c.state === ContainerState.MODIFIED;
}

function getDate<T>(c: Container<T>): number | undefined {
    switch(c.state) {
        case ContainerState.SYNCED:
        case ContainerState.MODIFIED:
        case ContainerState.DELETED:
            return c.modified;
        case ContainerState.LOADING:
            return c.timeStartedLoading;
        case ContainerState.ERRORED:
            return c.error.timeErrored;
        default:
            return undefined;
    }
}

/**
 * Sort by data, and then by date, showing empty container last
 */
function sort<T>(comparator: (a: T, b: T) => number): (a: Container<T>, b: Container<T>) => number {
    return (a, b) => {
        if(Container.isEmpty(a)) {
            if(Container.isEmpty(b)) {
                return 0;
            } else {
                return 1;
            }
        } else if(Container.isEmpty(b)) {
            return -1;
        }

        if(Container.isReady(a)) {
            if(Container.isReady(b)) {
                const dataCompare = comparator(a.data, b.data);
                if(dataCompare) {
                    return dataCompare;
                }
            } else {
                // a is before b
                return -1;
            }
        } else if(Container.isReady(b)) {
            return 1;
        }

        return (getDate(a) || 0) - (getDate(b) || 0);
    };
}

export const Container = {
    empty,
    loading,
    errored,
    synced,
    modified,
    deleted,
    isEmpty,
    isLoading,
    isErrored,
    isSynced,
    isModified,
    isDeleted,
    isReady,
    getDate,
    sort,
};

