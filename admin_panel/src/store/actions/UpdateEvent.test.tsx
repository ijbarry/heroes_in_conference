import {updateCachedEvent} from "./UpdateCachedEvent";
import {Container} from "../Container";
import {Event} from "../../events/Event";

describe("updateCachedEvent", () => {

    it("throws an error when no id is provided in any way", () => {
        expect(() => {
            updateCachedEvent(Container.empty());
        }).toThrow();
    });

    it("gets correct id when passed in through container", () => {
        const id = "my_id";

        const fullContainer = Container.synced({
            id,
            startTime: 0,
            endTime: 0,
            name: "My Event",
            description: "Yeet"
        } as Event, Date.now());

        const action = updateCachedEvent(fullContainer);

        expect(action.eventId).toEqual(id);
    });

});