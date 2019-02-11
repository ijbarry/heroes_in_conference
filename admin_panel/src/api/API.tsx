import {Event} from "../events/Event";
import {MockAPI} from "./MockAPI";

export interface API {

    // get all events from the server
    getEvents: () => Promise<Event[]>,

    // delete an event
    deleteEvent: (id: string) => Promise<void>,

    // update or create an event
    updateEvent: (event: Event) => Promise<void>,

}

export const API : API = MockAPI;