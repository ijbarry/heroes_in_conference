import {AppActionTypes} from "../../AppActions";
import {Action} from "redux";


export interface EventsLoadingAction extends Action<AppActionTypes> {
    type: AppActionTypes.EVENTS_LOADING,
}

/**
 * Update the internal state so we know we are loading the events
 */
export function eventsLoading(): EventsLoadingAction {
    return {
        type: AppActionTypes.EVENTS_LOADING,
    }
}