import {ThunkAction} from "redux-thunk";
import {AppState} from "./AppState";
import {EventsLoadedAction} from "./actions/EventsLoaded";
import {UpdateCachedEventAction} from "./actions/UpdateCachedEvent";
import {EventsLoadingAction} from "./actions/EventsLoading";
import {EventsLoadErrorAction} from "./actions/EventsLoadError";

// these are the values the type field of the action can take
export enum AppActionTypes {
    EVENTS_LOADED = "EVENTS_LOADED",
    UPDATE_EVENT = "UPDATE_EVENT",
    EVENTS_LOADING = "EVENTS_LOADING",
    EVENTS_LOAD_ERROR = "EVENTS_LOAD_ERROR",

}

// this way TypeScript can infer the type of an action from its "type" field
export type AppObjectAction =
    EventsLoadedAction
    | UpdateCachedEventAction
    | EventsLoadingAction
    | EventsLoadErrorAction
    ;

// type of our redux-thunk actions. Type params = thunk return type, state type, enhancer, 'object' action type
export type AppThunkAction = ThunkAction<any, AppState, any, AppObjectAction>;

// an app action can be an "object" action or a "thunk" (function) action
export type AppAction = AppObjectAction | AppThunkAction;