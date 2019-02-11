import {combineReducers} from "redux";
import {AppState} from "./AppState";
import {AppObjectAction} from "./AppActions";
import {reduceEvents} from "./reducers/events";
import {reduceAllEvents} from "./reducers/allEvents";


export const appReducer = combineReducers<AppState, AppObjectAction>({
    events: reduceEvents,
    allEvents: reduceAllEvents,
});