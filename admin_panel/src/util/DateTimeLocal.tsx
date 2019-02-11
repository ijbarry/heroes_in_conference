import {DateTime} from "../events/Event";
import {format, parse} from "date-fns";
import * as React from "react";
import {ChangeEvent} from "react";

const dateTimeLocalFormat = "yyyy-MM-dd'T'HH:mm";

// two functions for converting time to and from the formal expected by datetime-local inputs.

// would take way too long to test as would require faking the runtime's timezone to be consistent


export function toDateTimeLocal(date: DateTime): string {
    return format(date, dateTimeLocalFormat);
}

export function fromDateTimeLocal(input: string): DateTime {
    return parse(input, dateTimeLocalFormat, Date.now()).getTime();
}

interface InputProps {
    dateTime: number,
    onChange: (dateTime: number) => void,
    className: string,
}

interface State {
    value?: string,
}

type Props = InputProps;

export class DateTimeLocal extends React.Component<Props, State> {


    constructor(props: Readonly<Props>) {
        super(props);

        this.state = {};
    }

    public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>): void {
        if (prevProps.dateTime !== this.props.dateTime) {
            this.setState({
                value: toDateTimeLocal(this.props.dateTime),
            });
        }
    }

    public render(): React.ReactNode {
        const value = this.state.value || toDateTimeLocal(this.props.dateTime);

        return <input className={this.props.className} type="datetime-local"
                      value={value} onChange={this.onChange}/>
    }

    private onChange = (e: ChangeEvent<HTMLInputElement>) => {
        this.setState({
            value: e.target.value,
        });

        try {
            const timeValue = fromDateTimeLocal(e.target.value);
            if(timeValue) {
                this.props.onChange(timeValue);
            }
        } catch(ignored) {
            // ignore this exception
        }
    }

}