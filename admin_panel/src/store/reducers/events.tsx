import {AppActionTypes, AppObjectAction} from "../AppActions";
import {IDMap} from "../IDMap";
import {Event} from "../../events/Event"
import {Container} from "../Container";


export function reduceEvents(state: IDMap<Container<Event>> | undefined, action: AppObjectAction): IDMap<Container<Event>> {
    switch(action.type){
        case AppActionTypes.EVENTS_LOADED: {
            const newState : IDMap<Container<Event>> = {...state};
            const now = Date.now();

            for (const event of action.events) {
                newState[event.id] = Container.synced(event, now);
            }

            return newState;
        }
        case AppActionTypes.UPDATE_EVENT: {
            const newState : IDMap<Container<Event>> = {...state};

            newState[action.eventId] = action.event;

            return newState;
        }
        default: {
            return state || {};
        }
    }
}