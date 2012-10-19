/*
 * NetLink3Manager.h
 *
 * Copyright (C) 2011 IBR, TU Braunschweig
 *
 * Written-by: Johannes Morgenroth <morgenroth@ibr.cs.tu-bs.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

#ifndef NETLINK3MANAGER_H_
#define NETLINK3MANAGER_H_

#include "ibrcommon/net/LinkManager.h"
#include "ibrcommon/thread/Mutex.h"
#include "ibrcommon/thread/Thread.h"
#include "ibrcommon/net/vsocket.h"

#include <netlink/netlink.h>
#include <netlink/socket.h>
#include <netlink/route/link.h>
#include <netlink/msg.h>

namespace ibrcommon
{
	class netlink_callback {
	public:
		virtual ~netlink_callback() = 0;
		virtual void parse(struct nl_object *obj, int action) = 0;
	};

	class NetLink3ManagerEvent : public LinkManagerEvent
	{
	public:
		NetLink3ManagerEvent(EventType type, unsigned int state,
				ibrcommon::vinterface iface, ibrcommon::vaddress addr, bool wireless = false);
		virtual ~NetLink3ManagerEvent();

		virtual const ibrcommon::vinterface& getInterface() const;
		virtual const ibrcommon::vaddress& getAddress() const;
		virtual unsigned int getState() const;
		virtual EventType getType() const;

		virtual bool isWirelessExtension() const;

		const std::string toString() const;

	private:
		EventType _type;
		unsigned int _state;
		bool _wireless;
		ibrcommon::vinterface _interface;
		ibrcommon::vaddress _address;
	};

	class NetLink3Manager : public ibrcommon::LinkManager, public ibrcommon::JoinableThread
	{
		friend class LinkManager;

	public:
		virtual ~NetLink3Manager();

		const std::string getInterface(int index) const;
		const std::list<vaddress> getAddressList(const vinterface &iface);

		class parse_exception : public Exception
		{
		public:
			parse_exception(string error) : Exception(error)
			{};
		};

		void callback(const NetLink3ManagerEvent &evt);

	protected:
		void run();
		void __cancellation();

	private:
		NetLink3Manager();

		class netlinkcache : public basesocket, public netlink_callback
		{
		public:
			netlinkcache(int protocol, const std::string &name);
			virtual ~netlinkcache();
			virtual void up() throw (socket_exception);
			virtual void down() throw (socket_exception);

			virtual int fd() const throw (socket_exception);

			struct nl_cache* operator*() const throw (socket_exception);

			void receive() throw (socket_exception);

			virtual void parse(struct nl_object *obj, int flags);

		private:
			const int _protocol;
			const std::string _name;
			struct nl_sock *_nl_socket;
			struct nl_cache_mngr *_mngr;
			struct nl_cache *_cache;
		};

		ibrcommon::Mutex _cache_mutex;
		netlinkcache _route_cache;

		bool _running;

		ibrcommon::vsocket _sock;
	};
} /* namespace ibrcommon */
#endif /* NETLINK3MANAGER_H_ */
