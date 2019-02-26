import {Event} from './Event';

describe("Event.sortStartTime", () => {

    const eventA : Event = {
        startTime: 100,
        endTime: 200,
        name: "1_A",
        description: "",
        id: ""
    };

    const eventB : Event = {
        startTime: 200,
        endTime: 250,
        name: "2_B",
        description: "",
        id: ""
    };

    const eventC : Event = {
        startTime: 300,
        endTime: 400,
        name: "1_C",
        description: "",
        id: ""
    };

    const eventD : Event = {
        startTime: 100,
        endTime: 1000,
        name: "1_D",
        description: "",
        id: ""
    };

    const eventE : Event = {
        startTime: 300,
        endTime: 305,
        name: "2_E",
        description: "",
        id: ""
    };


    it("sorts A,B,C,D,E correctly", () => {
        let events = [eventC, eventB, eventA, eventE, eventD];
        events = events.sort(Event.sortStartTime);

        expect(events).toEqual([eventD, eventA, eventB, eventC, eventE]);
    })

});

describe("Event.validationMessage", () => {

    it("reports no name", () => {
        const namelessEvent : Event = {
            id: "id",
            name: "", // empty name
            description: "",
            startTime: new Date(2017, 1, 1).getTime(),
            endTime: new Date(2017, 1, 2).getTime(),
        };

        const msg = Event.validationMessage(namelessEvent);
        expect(typeof msg).toEqual("string");
    });

    it("reports starting after ending", () => {
        const backwardsEvent : Event = {
            id: "id",
            name: "Back to the Future",
            description: "Great Scott!!",
            startTime: new Date(2015, 10, 21).getTime(),
            endTime: new Date(1985, 12, 4).getTime()
        };

        const msg = Event.validationMessage(backwardsEvent);
        expect(typeof msg).toEqual("string");
    });

});