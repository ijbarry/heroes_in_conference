import {Event} from "../../../events/Event";
import {AppActionTypes} from "../../AppActions";
import {Action} from "redux";
import {Container} from "../../Container";

export interface UpdateCachedEventAction extends Action<AppActionTypes> {
    type: AppActionTypes.UPDATE_EVENT,
    eventId: string,
    event: Container<Event>,
}

/**
 * Updates or remove the cached copy of an event
 */
export function updateCachedEvent(event: Container<Event>, eventId?: string): UpdateCachedEventAction {
    let id = eventId;
    if(!id) {
        if(Container.isReady(event)) {
            id = event.data.id;
        } else {
            throw Error("No id provided to updateEvent explicitly or from container");
        }
    }

    return {
        type: AppActionTypes.UPDATE_EVENT,
        event,
        eventId: id
    }
}