import React from 'react'

export default class SelectInput extends React.Component {

    constructor(props) {
        super(props)

        this.state = {
            value: props.defaultValue,
            status: undefined
        }
    }

    handleChange(event) {
        let state = {}
        state[event.target.name] = event.target.value
        this.setState(state)
    }

    select(event) {
        let status = undefined
        let value = undefined
        if ('INPUT' === event.target.value) {
            status = event.target.value
        } else {
            value = event.target.value
            this.props.select(event.target.value)
        }
        this.setState({
            value: value,
            status: status
        })
    }

    create(event) {
        let value = event.target.value
        this.props.create(value)
        this.setState({
            value: value,
            status: undefined
        })
    }

    contentComponent = function() {
        let event = {target:{name:'status', value:'SELECT'}}
        return (
        <span onDoubleClick={ () => this.handleChange(event) }>
            { this.props.children }
        </span>
        )
    }

    selectComponent = function() {
        return (
        <select
            onChange={ (event) => this.select(event) }
            onBlur={ (event) => this.select(event) }
            value={ this.state.value }>
            {
                this.props.options.map((option, index) =>
                    <option
                        key={ option.key + index }
                        value={ option.key }>
                        { option.value }
                    </option>
                )
            }
            <option value="INPUT">{ this.props.placeholder }</option>
        </select>
        )
    }

    inputComponent = function() {
        return (
        <input
            type="text"
            placeholder={ this.props.placeholder }
            onBlur={ (event) => this.create(event) } />
        )
    }

    render() {
        if (this.state.status === undefined) {
            return this.contentComponent()
        } else if (this.state.status === 'SELECT') {
            return this.selectComponent()
        } else if (this.state.status === 'INPUT') {
            return this.inputComponent()
        }
    }

}