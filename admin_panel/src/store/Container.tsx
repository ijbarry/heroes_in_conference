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

export type Container<T> =
    EmptyContainer
    | LoadingContainer
    | ErroredContainer
    | SyncedContainer<T>
    | ModifiedContainer<T>;


function isEmpty<T>(c : Container<T>) : c is EmptyContainer {
    return c.state === ContainerState.EMPTY;
}

function isLoading<T>(c : Container<T>) : c is LoadingContainer {
    return c.state === ContainerState.LOADING;
}

// did we error while loading, not did we error while trying to save data
function isErrored<T>(c : Container<T>) : c is ErroredContainer {
    return c.state === ContainerState.ERRORED;
}

function isSynced<T>(c : Container<T>) : c is SyncedContainer<T> {
    return c.state === ContainerState.SYNCED;
}

function isModified<T>(c : Container<T>) : c is ModifiedContainer<T> {
    return c.state === ContainerState.MODIFIED;
}

function isReady<T>(c : Container<T>) : c is SyncedContainer<T> | ModifiedContainer<T> {
    return c.state === ContainerState.SYNCED || c.state === ContainerState.MODIFIED;
}

export const Container = {
    empty,
    loading,
    errored,
    synced,
    modified,
    isEmpty,
    isLoading,
    isErrored,
    isSynced,
    isModified,
    isReady,
};

