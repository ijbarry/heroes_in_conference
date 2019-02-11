import {AppThunkAction} from "../AppActions";
import {eventsLoading} from "./EventsLoading";
import {API} from "../../api/API";
import {eventsLoaded} from "./EventsLoaded";
import {eventsLoadError} from "./EventsLoadError";


/**
 * Attempt to load all events from the server
 */
export function loadEvents(): AppThunkAction {
    return dispatch => {

        // start loading events
        dispatch(eventsLoading());

        API.getEvents()
            .then(events => {
                // load success
                dispatch(eventsLoaded(events));
            })
            .catch(reason => {
                // load failed
                dispatch(eventsLoadError(reason));
            });
    }
}