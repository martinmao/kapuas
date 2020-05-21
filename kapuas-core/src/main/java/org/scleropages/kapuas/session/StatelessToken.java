/**
 * Copyright 2001-2005 The Apache Software Foundation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scleropages.kapuas.session;

/**
 * <pre>
 * 标识接口，标识无需为该token创建session,认证提供程序处理该类型的token不应创建session上下文。
 * 服务端为实现更好的扩容，进行无状态化，即身份信息由token自身提供。但必须确保采用安全的签名算法（HmacSHAx，rsa等）进行签名，防止篡改。
 * 且对于敏感信息采用其他加密协议（如https）。
 * </pre>
 * <pre>
 * 无状态token机制需权衡以下问题：
 * 服务端对token控制存在缺陷（只能被动接收并验签），要实现在线会话操作还需集中式会话方案
 * 复杂场景需客户端配合执行（但涉及秘钥如何安全发布给客户端）。
 * </pre>
 * <pre>
 * 目前较为理想的使用方案为：将会话信息仅共享给认证，鉴权服务，避免所有服务使用集中式会话
 * 1.app（h5）用户认证基于集中式session管理，返回cookie（或sessionId token），客户端缓存
 * 2.客户端服务调用前先访问鉴权服务询问访问权限，鉴权服务根据会话信息鉴权（与认证服务共享会话），
 *   鉴权通过则返回StatelessToken（经签名，防篡改）
 * 3.业务服务对StatelessToken仅执行验签，成功则获取必要信息（用户标识等）直接执行业务
 * NOTE：设计上应对每个业务服务使用不同秘钥，且鉴权服务返回的token应包含时间戳信息避免重放攻击
 * </pre>
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public interface StatelessToken {

}
