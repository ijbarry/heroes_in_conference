import {AppActionTypes} from "../AppActions";
import {Action} from "redux";
import {ErrorState} from "../Container";


export interface EventsLoadErrorAction extends Action<AppActionTypes> {
    type: AppActionTypes.EVENTS_LOAD_ERROR,
    error: ErrorState,
}

/**
 * Record an error that occurs when trying to load all events
 */
export function eventsLoadError(error: ErrorState): EventsLoadErrorAction {
    return {
        type: AppActionTypes.EVENTS_LOAD_ERROR,
        error,
    }
}