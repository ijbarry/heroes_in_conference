import {Container} from "../Container";
import {AppActionTypes, AppObjectAction} from "../AppActions";

export function reduceAllEvents(state: Container<{}> | undefined, action: AppObjectAction) : Container<{}> {
    switch(action.type) {
        case AppActionTypes.EVENTS_LOADED:
            return Container.synced({}, Date.now());
        case AppActionTypes.EVENTS_LOADING:
            return Container.loading(Date.now());
        case AppActionTypes.EVENTS_LOAD_ERROR:
            return Container.errored(action.error);
        default:
            return state || Container.empty();
    }
}