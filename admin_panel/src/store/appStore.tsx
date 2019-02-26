import {applyMiddleware, createStore} from 'redux';
import {AppState} from "./AppState";
import {AppObjectAction} from "./AppActions";
import thunk, {ThunkDispatch} from "redux-thunk";
import {appReducer} from "./appReducer";

// type of the dispatch
export type AppDispatch = ThunkDispatch<AppState, any, AppObjectAction>;

export const appStore = createStore<AppState, AppObjectAction, {}, {}>(appReducer, {}, applyMiddleware(thunk));