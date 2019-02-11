import {Container, ModifiedContainer} from "../Container";
import {updateCachedEvent} from "./UpdateCachedEvent";
import {Event} from "../../events/Event";
import {API} from "../../api/API";
import {AppDispatch} from "../appStore";

/**
 * Attempts to post event to server, and, if successful, updates the event in the cache.
 */
export function updateEvent(event: ModifiedContainer<Event>, dispatch: AppDispatch): Promise<void> {
    dispatch(updateCachedEvent(event));

    return API.updateEvent(event.data).then(value => {
        // we are now in sync
        dispatch(updateCachedEvent(Container.synced(event.data, event.modified)));
    });
}