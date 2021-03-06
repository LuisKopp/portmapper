/**
 * UPnP PortMapper - A tool for managing port forwardings via UPnP
 * Copyright (C) 2015 Christoph Pirkl <christoph at users.sourceforge.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.chris.portmapper.router.cling.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.chris.portmapper.router.cling.ClingRouterException;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.ActionArgument;
import org.fourthline.cling.model.meta.ActionArgument.Direction;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.meta.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractClingAction<T> implements ClingAction<T> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Service<RemoteDevice, RemoteService> service;
    private final String actionName;

    public AbstractClingAction(final Service<RemoteDevice, RemoteService> service, final String actionName) {
        this.service = service;
        this.actionName = actionName;
    }

    public Map<String, Object> getArgumentValues() {
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ActionInvocation<RemoteService> getActionInvocation() {
        final Action<RemoteService> action = service.getAction(actionName);
        if (action == null) {
            throw new ClingRouterException("No action found for name '" + actionName + "'. Available actions: "
                    + Arrays.toString(service.getActions()));
        }
        @SuppressWarnings("rawtypes")
        final ActionArgumentValue[] argumentArray = getArguments(action);
        return new ActionInvocation<RemoteService>(action, argumentArray);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private ActionArgumentValue[] getArguments(final Action<RemoteService> action) {
        final ActionArgument[] actionArguments = action.getArguments();
        final Map<String, Object> argumentValues = getArgumentValues();
        final List<ActionArgumentValue<RemoteService>> actionArgumentValues = new ArrayList<>(actionArguments.length);

        for (final ActionArgument<RemoteService> actionArgument : actionArguments) {
            if (actionArgument.getDirection() == Direction.IN) {
                final Object value = argumentValues.get(actionArgument.getName());
                logger.trace("Action {}: add arg value for {}: {} (expected datatype: {})", action.getName(),
                        actionArgument, value, actionArgument.getDatatype().getDisplayString());
                actionArgumentValues.add(new ActionArgumentValue<>(actionArgument, value));
            }
        }
        return actionArgumentValues.toArray(new ActionArgumentValue[actionArgumentValues.size()]);
    }
}