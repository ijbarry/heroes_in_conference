import {API} from "../../../api/API";
import {updateCachedEvent} from "./UpdateCachedEvent";
import {Container} from "../../Container";
import {Event} from "../../../events/Event";
import {AppDispatch} from "../../appStore";


/**
 * Deletes an event server side. If successful also removes the event from the cache.
 */
export function deleteEvent(event: Event, dispatch: AppDispatch): Promise<void> {
    return API.deleteEvent(event.id).then(value => {
        dispatch(updateCachedEvent(Container.empty(), event.id));
    });
}